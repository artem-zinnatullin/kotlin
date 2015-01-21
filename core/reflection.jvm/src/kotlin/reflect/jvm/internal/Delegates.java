/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.reflect.jvm.internal;

import kotlin.Function0;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;

/* package */ class Delegates {
    // A delegate for a lazy property on a soft reference.
    // NOTE: its initializer may be invoked multiple times including simultaneously from different threads
    public static class LazySoftVal<T> {
        private static final Object NULL_VALUE = new Object() {};

        private final Function0<T> initializer;
        private SoftReference<Object> value = null;

        public LazySoftVal(@NotNull Function0<T> initializer) {
            this.initializer = initializer;
        }

        @SuppressWarnings("UnusedParameters")
        public T get(Object instance, Object metadata) {
            if (value != null) {
                Object result = value.get();
                if (result != null) {
                    return unescape(result);
                }
            }

            T result = initializer.invoke();
            value = new SoftReference<Object>(escape(result));

            return result;
        }

        private Object escape(T value) {
            return value == null ? NULL_VALUE : value;
        }

        @SuppressWarnings("unchecked")
        private T unescape(Object value) {
            return value == NULL_VALUE ? null : (T) value;
        }
    }

    @NotNull
    public static <T> LazySoftVal<T> lazySoft(@NotNull Function0<T> initializer) {
        return new LazySoftVal<T>(initializer);
    }
}
