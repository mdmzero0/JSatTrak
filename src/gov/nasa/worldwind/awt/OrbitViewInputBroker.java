/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.ScheduledOrbitViewStateIterator;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: OrbitViewInputBroker.java 3548 2007-11-16 06:27:16Z dcollins $
 */
public class OrbitViewInputBroker
        implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener
{
    private OrbitView view;
    private WorldWindow wwd;
    private boolean smoothViewChanges = true;
    private boolean lockHeading = true;
    // Current mouse state.
    private java.awt.Point mousePoint = null;
    private Position selectedPosition = null;
    private final Integer[] POLLED_KEYS =
        {
            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_PAGE_UP,
            KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_ADD, KeyEvent.VK_EQUALS,
            KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS
        };
    private KeyPollTimer keyPollTimer = new KeyPollTimer(25, Arrays.asList(POLLED_KEYS),
        new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (actionEvent == null)
                    return;

                Object source = actionEvent.getSource();
                if (source == null || !(source instanceof Integer))
                    return;

                keyPolled((Integer) source, actionEvent.getModifiers());
            }
        });

    public OrbitViewInputBroker()
    {
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow newWorldWindow)
    {
        this.wwd = newWorldWindow;

        if (this.wwd == null || this.wwd.getView() == null || !(this.wwd.getView() instanceof OrbitView))
            this.view = null;
        else
            this.view = (OrbitView) this.wwd.getView();
    }

    public boolean isSmoothViewChanges()
    {
        return this.smoothViewChanges;
    }

    public void setSmoothViewChanges(boolean smoothViewChanges)
    {
        this.smoothViewChanges = smoothViewChanges;
    }

    public boolean isLockHeading()
    {
        return this.lockHeading;
    }

    public void setLockHeading(boolean lockHeading)
    {
        this.lockHeading = lockHeading;
    }

    private Point getMousePoint()
    {
        return this.mousePoint;
    }

    private void updateMousePoint(MouseEvent event)
    {
        if (event != null)
        {
            if (this.wwd instanceof Component)
                this.mousePoint = constrainPointToComponentBounds(event.getX(), event.getY(), (Component) this.wwd);
            else
                this.mousePoint = new Point(event.getX(), event.getY());
        }
        else
        {
            this.mousePoint = null;
        }
    }

    private Point constrainPointToComponentBounds(int x, int y, Component c)
    {
        if (c != null)
        {
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;

            if (x > c.getWidth())
                x = c.getWidth();
            if (y > c.getHeight())
                y = c.getHeight();
        }

        return new Point(x, y);
    }

    private Position getSelectedPosition()
    {
        return this.selectedPosition;
    }

    private boolean hasSelectedPosition()
    {
        return this.selectedPosition != null;
    }

    private void updateSelectedPosition()
    {
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null
         && pickedObjects.getTopPickedObject() != null
         && pickedObjects.getTopPickedObject().isTerrain())
        {
            this.selectedPosition = pickedObjects.getTopPickedObject().getPosition();
        }
        else
        {
            this.selectedPosition = null;
        }
    }

    private void clearSelectedPosition()
    {
        this.selectedPosition = null;
    }

    private Position computePositionAtPoint(double mouseX, double mouseY)
    {
        Position position = null;
        
        // Attempt to intersect with sphere/ellipsoid passing through selected position.
        if (this.selectedPosition != null
         && this.wwd.getModel() != null
         && this.wwd.getModel().getGlobe() != null)
        {
            Globe globe = this.wwd.getModel().getGlobe();
            Line line = this.view.computeRayFromScreenPoint(mouseX, mouseY);
            if (line != null)
            {
                // Ratio of radii at selected lat/lon.
                double ratio = 1 + (this.selectedPosition.getElevation() / globe.getRadiusAt(this.selectedPosition.getLatLon()));
                // Scaled equatorial and polar radii.
                double equRadius = ratio * globe.getEquatorialRadius();
                double polRadius = ratio * globe.getPolarRadius();
                // Intersect with scaled ellipsoid (the ellipsoid passing through the selected position).
                Intersection[] intersection = intersect(line, globe.getCenter(), equRadius, polRadius);
                if (intersection != null && intersection.length > 0)
                {
                    position = globe.computePositionFromPoint(intersection[0].getIntersectionPoint());
                }
            }
        }
        // Fallback to globe intersection.
        else
        {
            position = this.view.computePositionFromScreenPoint(mouseX, mouseY);
        }

        return position;
    }

    private Intersection[] intersect(Line line, Vec4 origin, double equRadius, double polRadius)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Taken from Lengyel, 2Ed., Section 5.2.3, page 148.

        double m = equRadius / polRadius; // "ratio of the x semi-axis length to the y semi-axis length"
        double n = 1d;                    // "ratio of the x semi-axis length to the z semi-axis length"
        double m2 = m * m;
        double n2 = n * n;
        double r2 = equRadius * equRadius; // nominal radius squared //equRadius * polRadius;

        double vx = line.getDirection().x;
        double vy = line.getDirection().y;
        double vz = line.getDirection().z;
        double sx = line.getOrigin().x - origin.x;
        double sy = line.getOrigin().y - origin.y;
        double sz = line.getOrigin().z - origin.z;

        double a = vx * vx + m2 * vy * vy + n2 * vz * vz;
        double b = 2d * (sx * vx + m2 * sy * vy + n2 * sz * vz);
        double c = sx * sx + m2 * sy * sy + n2 * sz * sz - r2;

        double discriminant = discriminant(a, b, c);
        if (discriminant < 0)
            return null;

        double discriminantRoot = Math.sqrt(discriminant);
        if (discriminant == 0)
        {
            Vec4 p = line.getPointAt((-b - discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(p, true)};
        }
        else // (discriminant > 0)
        {
            Vec4 near = line.getPointAt((-b - discriminantRoot) / (2 * a));
            Vec4 far = line.getPointAt((-b + discriminantRoot) / (2 * a));

            if (c < 0) // Line originates inside the ellipsoid
            {
                return new Intersection[] {new Intersection(far, false)};
            }
            else // Standard case 
            {
                return new Intersection[] {new Intersection(near, false), new Intersection(far, false)};
            }
        }
    }

    static private double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    public void keyPolled(int keyCode, int modifiers)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        int slowMask = (modifiers & InputEvent.ALT_DOWN_MASK);
        boolean slow = slowMask != 0x0;

        if (areModifiersExactly(modifiers, slowMask))
        {
            if (isLockHeading())
            {
                double sinHeading = view.getHeading().sin();
                double cosHeading = view.getHeading().cos();
                double latFactor = 0;
                double lonFactor = 0;
                if (keyCode == KeyEvent.VK_LEFT)
                {
                    latFactor = sinHeading;
                    lonFactor = -cosHeading;
                }
                else if (keyCode == KeyEvent.VK_RIGHT)
                {
                    latFactor = -sinHeading;
                    lonFactor = cosHeading;
                }
                else if (keyCode == KeyEvent.VK_UP)
                {
                    latFactor = cosHeading;
                    lonFactor = sinHeading;
                }
                else if (keyCode == KeyEvent.VK_DOWN)
                {
                    latFactor = -cosHeading;
                    lonFactor = -sinHeading;
                }
                if (latFactor != 0 || lonFactor != 0)
                {
                    Globe globe = this.wwd.getModel().getGlobe();
                    if (globe != null)
                    {
                        Angle latChange = this.computeLatOrLonChange(this.view, globe, latFactor, slow);
                        Angle lonChange = this.computeLatOrLonChange(this.view, globe, lonFactor, slow);
                        LatLon lookAt = new LatLon(
                            this.view.getLookAtLatitude().add(latChange),
                            this.view.getLookAtLongitude().add(lonChange));
                        this.setLookAtLatLon(this.view, lookAt);
                        return;
                    }
                }
            }
            else
            {
                double forwardAmount = 0;
                double rightAmount = 0;
                if (keyCode == KeyEvent.VK_LEFT)
                    rightAmount = -1;
                else if (keyCode == KeyEvent.VK_RIGHT)
                    rightAmount = 1;
                else if (keyCode == KeyEvent.VK_UP)
                    forwardAmount = 1;
                else if (keyCode == KeyEvent.VK_DOWN)
                    forwardAmount = -1;

                if (forwardAmount != 0 || rightAmount != 0)
                {
                    Globe globe = this.wwd.getModel().getGlobe();
                    if (globe != null)
                    {
                        Angle forwardAngle = this.computeLatOrLonChange(this.view, globe, forwardAmount, slow);
                        Angle rightAngle = this.computeLatOrLonChange(this.view, globe, rightAmount, slow);
                        Quaternion forwardQuat = this.view.createRotationForward(forwardAngle);
                        Quaternion rightQuat = this.view.createRotationRight(rightAngle);
                        Quaternion quaternion = forwardQuat.multiply(rightQuat);
                        Quaternion rotation = this.computeNewRotation(this.view, quaternion);
                        this.setRotation(this.view, rotation);
                        return;
                    }
                }
            }
        }

        double headingFactor = 0;
        double pitchFactor = 0;
        if (areModifiersExactly(modifiers, slowMask))
        {
            if (keyCode == KeyEvent.VK_PAGE_DOWN)
                pitchFactor = 1;
            else if (keyCode == KeyEvent.VK_PAGE_UP)
                pitchFactor = -1;
        }
        else if (areModifiersExactly(modifiers, InputEvent.SHIFT_DOWN_MASK | slowMask))
        {
            if (keyCode == KeyEvent.VK_LEFT)
                headingFactor = -1;
            else if (keyCode == KeyEvent.VK_RIGHT)
                headingFactor = 1;
            else if (keyCode == KeyEvent.VK_UP)
                pitchFactor = -1;
            else if (keyCode == KeyEvent.VK_DOWN)
                pitchFactor = 1;
        }
        if (headingFactor != 0)
        {
            Angle newHeading = this.computeNewHeading(this.view, 4 * headingFactor, slow);
            Angle newPitch = this.view.getPitch();
            this.setHeadingAndPitch(this.view, newHeading, newPitch);
            return;
        }
        else if (pitchFactor != 0)
        {
            Angle newHeading = this.view.getHeading();
            Angle newPitch = this.computeNewPitch(this.view, 4 * pitchFactor, slow);
            this.setHeadingAndPitch(this.view, newHeading, newPitch);
            return;
        }

        double zoomFactor = 0;
        if (areModifiersExactly(modifiers, slowMask))
        {
            if (keyCode == KeyEvent.VK_ADD ||
                keyCode == KeyEvent.VK_EQUALS)
                zoomFactor = -1;
            else if (keyCode == KeyEvent.VK_SUBTRACT ||
                keyCode == KeyEvent.VK_MINUS)
                zoomFactor = 1;
        }
        else if (areModifiersExactly(modifiers, InputEvent.CTRL_DOWN_MASK | slowMask)
            || areModifiersExactly(modifiers, InputEvent.META_DOWN_MASK | slowMask))
        {
            if (keyCode == KeyEvent.VK_UP)
                zoomFactor = -1;
            else if (keyCode == KeyEvent.VK_DOWN)
                zoomFactor = 1;
        }
        if (zoomFactor != 0)
        {
            double newZoom = this.computeNewZoom(this.view, zoomFactor, slow);
            this.setZoom(this.view, newZoom);
        }
    }

    public void keyTyped(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyTyped(keyEvent);
    }

    public void keyPressed(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyPressed(keyEvent);
    }

    public void keyReleased(KeyEvent keyEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (keyEvent == null)
            return;

        this.keyPollTimer.keyReleased(keyEvent);

        int keyCode = keyEvent.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE)
        {
            if (this.view.hasStateIterator())
                this.view.stopStateIterators();
        }
        else if (keyCode == KeyEvent.VK_N)
        {
            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingIterator(
                this.view,
                Angle.ZERO)); // Reset heading.
        }
        else if (keyCode == KeyEvent.VK_R)
        {
            this.view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingAndPitchIterator(
                this.view,
                Angle.ZERO,   // Reset heading.
                Angle.ZERO)); // Reset pitch.
        }
    }

    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects == null
            || pickedObjects.getTopPickedObject() == null
            || !pickedObjects.getTopPickedObject().isTerrain())
            return;

        PickedObject top = pickedObjects.getTopPickedObject();
        Position topPosition = top.getPosition();

        final double SMOOTHING = 0.9;
        final boolean DO_COALESCE = false;
        if (isLockHeading())
        {
            LatLon latLon = new LatLon(topPosition.getLatitude(), topPosition.getLongitude());
            this.view.applyStateIterator(OrbitViewInputStateIterator.createLookAtLatLonIterator(
                latLon, SMOOTHING, DO_COALESCE));
        }
        else
        {
            Quaternion quaternion = this.view.createRotationBetweenPositions(
                topPosition,
                new Position(this.view.getLookAtLatitude(), this.view.getLookAtLongitude(), 0));
            Quaternion rotation = this.computeNewRotation(this.view, quaternion);
            this.view.applyStateIterator(OrbitViewInputStateIterator.createRotationIterator(
                rotation, SMOOTHING, DO_COALESCE));
        }
    }

    public void mousePressed(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        updateMousePoint(mouseEvent);
        updateSelectedPosition();
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        updateMousePoint(mouseEvent);
        clearSelectedPosition();
    }

    public void mouseEntered(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void mouseExited(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void mouseDragged(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        // Store previous mouse point.
        java.awt.Point prevMousePoint = getMousePoint();
        // Update mouse point.
        updateMousePoint(mouseEvent);
        // Store new mouse point.
        java.awt.Point curMousePoint = getMousePoint();
        // Compute mouse movement.
        java.awt.Point mouseMove = null;
        if (curMousePoint != null && prevMousePoint != null)
        {
            mouseMove = new java.awt.Point(
                curMousePoint.x - prevMousePoint.x,
                curMousePoint.y - prevMousePoint.y);
        }
        // Compute the current selected position if none exists.
        if (!this.hasSelectedPosition())
        {
            this.updateSelectedPosition();
        }

        if (areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK))
        {
            if (prevMousePoint != null && curMousePoint != null)
            {
                Position prevPosition = computePositionAtPoint(prevMousePoint.x, prevMousePoint.y);
                Position curPosition = computePositionAtPoint(curMousePoint.x, curMousePoint.y);
                // Keep selected position under cursor.
                if (prevPosition != null && curPosition != null)
                {
                    if (!prevPosition.equals(curPosition))
                    {
                        if (isLockHeading())
                        {
                            LatLon lookAt = new LatLon(
                                this.view.getLookAtLatitude().add(prevPosition.getLatitude()).subtract(curPosition.getLatitude()),
                                this.view.getLookAtLongitude().add(prevPosition.getLongitude()).subtract(curPosition.getLongitude()));
                            this.setLookAtLatLon(this.view, lookAt);
                        }
                        else
                        {
                            Quaternion quaternion = this.view.createRotationBetweenPositions(prevPosition, curPosition);
                            if (quaternion != null)
                            {
                                Quaternion rotation = this.computeNewRotation(this.view, quaternion);
                                this.setRotation(this.view, rotation);
                            }
                        }
                    }
                }
                // Cursor is off the globe, simulate globe dragging.
                else
                {
                    Globe globe = this.wwd.getModel().getGlobe();
                    if (globe != null)
                    {
                        if (isLockHeading())
                        {
                            double sinHeading = this.view.getHeading().sin();
                            double cosHeading = this.view.getHeading().cos();
                            double latFactor = (cosHeading * mouseMove.y + sinHeading * mouseMove.x) / 10.0;
                            double lonFactor = (sinHeading * mouseMove.y - cosHeading * mouseMove.x) / 10.0;
                            Angle latChange = this.computeLatOrLonChange(this.view, globe, latFactor, false);
                            Angle lonChange = this.computeLatOrLonChange(this.view, globe, lonFactor, false);
                            LatLon lookAt = new LatLon(
                                this.view.getLookAtLatitude().add(latChange),
                                this.view.getLookAtLongitude().add(lonChange));
                            this.setLookAtLatLon(this.view, lookAt);
                        }
                        else
                        {
                            double forwardFactor = mouseMove.y / 10.0;
                            double rightFactor = -mouseMove.x / 10.0;
                            Angle forwardAngle = this.computeLatOrLonChange(this.view, globe, forwardFactor, false);
                            Angle rightAngle = this.computeLatOrLonChange(this.view, globe, rightFactor, false);
                            Quaternion forwardQuat = this.view.createRotationForward(forwardAngle);
                            Quaternion rightQuat = this.view.createRotationRight(rightAngle);
                            Quaternion quaternion = forwardQuat.multiply(rightQuat);
                            Quaternion rotation = this.computeNewRotation(this.view, quaternion);
                            this.setRotation(this.view, rotation);
                        }
                    }

                    // Cursor went off the globe. Clear the selected position to ensure a new one will be
                    // computed if the cursor returns to the globe.
                    clearSelectedPosition();
                }
            }
        }
        else if (areModifiersExactly(mouseEvent, InputEvent.BUTTON3_DOWN_MASK)
            || areModifiersExactly(mouseEvent, InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK))
        {
            if (mouseMove != null)
            {
                // Switch the direction of heading change depending on whether the cursor is above or below
                // the center of the screen.
                double headingDirection = 1;
                Object source = mouseEvent.getSource();
                if (source != null && source instanceof java.awt.Component)
                {
                    java.awt.Component component = (java.awt.Component) source;
                    if (mouseEvent.getPoint().y < (component.getHeight() / 2))
                        headingDirection = -1;
                }
    
                Angle newHeading = this.computeNewHeading(this.view, headingDirection * mouseMove.x, false);
                Angle newPitch = this.computeNewPitch(this.view, mouseMove.y, false);
                this.setHeadingAndPitch(this.view, newHeading, newPitch);
            }
        }
    }

    public void mouseMoved(MouseEvent mouseEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseEvent == null)
            return;

        updateMousePoint(mouseEvent);
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (mouseWheelEvent == null)
            return;

        int wheelRotation = mouseWheelEvent.getWheelRotation();
        double wheelDirection = Math.signum(wheelRotation);
        double newZoom = this.computeNewZoom(this.view, wheelDirection, false);
        this.setZoom(this.view, newZoom);
    }

    public void focusGained(FocusEvent focusEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;
    }

    public void focusLost(FocusEvent focusEvent)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
            return;

        if (this.view == null) // include this test to ensure any derived implementation performs it
            return;

        if (focusEvent == null)
            return;

        this.view.stopStateIterators();
        this.keyPollTimer.stop();
    }

    private static boolean areModifiersExactly(InputEvent inputEvent, int mask)
    {
        return areModifiersExactly(inputEvent.getModifiersEx(), mask);
    }

    private static boolean areModifiersExactly(int modifiersEx, int mask)
    {
        return modifiersEx == mask;
    }

    // ============== View State Changes ======================= //
    // ============== View State Changes ======================= //
    // ============== View State Changes ======================= //

    private void updateView(View view)
    {
        if (view.hasStateIterator())
            view.stopStateIterators();

        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    private void setLookAtLatLon(OrbitView view, LatLon lookAtLatLon)
    {
        if (this.isSmoothViewChanges())
        {
            view.applyStateIterator(OrbitViewInputStateIterator.createLookAtLatLonIterator(lookAtLatLon));
        }
        else
        {
            view.setLookAtLatLon(lookAtLatLon);
            this.updateView(view);
        }
    }

    private Angle computeLatOrLonChange(View view, Globe globe, double amount, boolean slow)
    {
        Position eyePos = view.getEyePosition();
        if (eyePos == null)
            return null;

        double normAlt = (eyePos.getElevation() / globe.getRadiusAt(eyePos.getLatLon()));
        if (normAlt < 0)
            normAlt = 0;
        else if (normAlt > 1)
            normAlt = 1;

        double coeff = (0.0001 * (1 - normAlt)) + (2 * normAlt);
        if (slow)
            coeff /= 4.0;

        return Angle.fromDegrees(coeff * amount);
    }

    private void setHeadingAndPitch(OrbitView view, Angle heading, Angle pitch)
    {
        if (this.isSmoothViewChanges())
        {
            view.applyStateIterator(OrbitViewInputStateIterator.createHeadingAndPitchIterator(
                heading,
                pitch));
        }
        else
        {
            view.setHeading(heading);
            view.setPitch(pitch);
            this.updateView(view);
        }
    }

    private Angle computeNewHeadingOrPitch(Angle value, double amount, boolean slow)
    {
        double coeff = 1.0/4.0;
        if (slow)
            coeff /= 4.0;

        Angle change = Angle.fromDegrees(coeff * amount);
        return value.add(change);
    }

    private Angle computeNewHeading(OrbitView view, double amount, boolean slow)
    {
        return computeNewHeadingOrPitch(view.getHeading(), amount, slow);
    }

    private Angle computeNewPitch(OrbitView view, double amount, boolean slow)
    {
        return computeNewHeadingOrPitch(view.getPitch(), amount, slow);
    }

    private void setZoom(OrbitView view, double zoom)
    {
        if (this.isSmoothViewChanges())
        {
            view.applyStateIterator(OrbitViewInputStateIterator.createZoomIterator(
                zoom));
        }
        else
        {
            view.setZoom(zoom);
            this.updateView(view);
        }
    }

    private double computeNewZoom(OrbitView view, double amount, boolean slow)
    {
        double coeff = 0.05;
        if (slow)
            coeff /= 4.0;

        double change = coeff * amount;
        double zoom = view.getZoom();
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // 1) Zooming is slow near the globe, and fast at great distances.
        // 2) Zooming in then immediately zooming out returns the viewer to the same zoom value.
        return Math.exp(Math.log(zoom) + change);
    }

    private void setRotation(OrbitView view, Quaternion rotation)
    {
        if (this.isSmoothViewChanges())
        {
            view.applyStateIterator(OrbitViewInputStateIterator.createRotationIterator(
                rotation));
        }
        else
        {
            view.setRotation(rotation);
            this.updateView(view);
        }
    }

    private Quaternion computeNewRotation(OrbitView view, Quaternion amount)
    {
        return view.getRotation().multiply(amount);
    }
}
