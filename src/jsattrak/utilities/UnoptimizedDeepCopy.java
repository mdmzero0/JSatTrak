/*
 * UnoptimizedDeepCopy.java
 * 
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * 
 * From: http://javatechniques.com/blog/faster-deep-copies-of-java-objects/ by Philip Isenhour
 * 
 */

package jsattrak.utilities;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception.
 *
 * A later version of this class includes some minor optimizations.
 */
public class UnoptimizedDeepCopy {

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     * @param orig
     * @return 
     */
    public static Object copy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

}
