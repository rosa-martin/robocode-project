/**
 * Visual Recognition API for Java, JSR381
 * Copyright (C) 2020  Zoran Sevarac, Frank Greco
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package javax.visrec.spi;

import javax.visrec.ImageFactory;
import java.util.Optional;

/**
 * The service to locate and find implementations of the {@link ImageFactory} interface.
 *
 * @since 1.0
 */
public interface ImageFactoryService {

    /**
     * Get the {@link ImageFactory} implementation by its image type.
     *
     * @param imageCls image type in {@link Class} object which is able to
     *                 be processed by the image factory implementation.
     * @param <T>      image type
     * @return {@link Optional} with possible {@link ImageFactory} implementation
     * if found.
     */
    <T> Optional<ImageFactory<T>> getByImageType(Class<T> imageCls);

}
