# overload-fn

**overload-fn** is a tiny Clojure library that makes possible function overloading on type. Since Clojure doesn't support function overloading based on type hints, you need to either create a **protocol** or **multi-method**. This is where the library comes into play. Based on your overloaded function declaration, overload-fn chooses protocol or multi-method to implement overloading functionality.

When working with Java libraries (especially writing wrappers), creating multiple protocols and multi-methods becomes daunting, so the library aims to solve this problem.

## Installation
[![Clojars Project](https://clojars.org/overload-fn/latest-version.svg)](https://clojars.org/overload-fn)

## Usage
```clojure
(require '[overload-fn.core :as of])

(of/defn add
  ([^Double x y z]
   (+ x y z))
  ([^Long x y z]
   (+ x y z 2)))

(add 0.5 1 1) ;; 2.5
(add 1 1 1) ;; 5
```

```clojure
(of/defn my-fn
  ([^Double x]
   :double)
  ([^Long x]
   :long)
  ([x]
   :any-type))

(my-fn 12.2) ;; :double
(my-fn 5) ;; :long
(my-fn nil) ;; :any-type
(my-fn {:a 1}) ;; :any-type
```

```clojure
(of/defn multi-args-fn
  ([^Long x ^String y ^Long z]
   [:long :str :long])
  ([^Long x ^String y ^Number z]
   [:long :str :number])
  ([^Long x ^String y z]
   [:long :str :any]))

(multi-args-fn 1 "hey" 2) ;; [:long :str :long]
(multi-args-fn 1 "hey" 5.5) ;; [:long :str :number]
(multi-args-fn 1 "hey" :dude) ;; [:long :str :any]
(multi-args-fn 1 "hey" {:a 1}) ;; [:long :str :any]
(multi-args-fn 1 "hey" nil) ;; [:long :str :any]
```
### Before & After
`Vector2f` and `Vecto3f` are used in JMonkeyEngine Game Engine.

#### Before
```clojure
(defprotocol Matrix
  (add [this v] [this x y] [this x y z]))

(extend-protocol Matrix
  Vector3f
  (add
    ([this v]
     (.add this v))
    ([this x y z]
     (.add this x y z)))

  Vector2f
  (add
    ([this v]
     (.add this v))
    ([this x y]
     (.add this x y))))
```
#### After
```clojure
(of/defn add
  ([^Vector2f this v]
   (.add this v))
  ([^Vector3f this v]
   (.add this v))
  ([^Vector2f this x y]
   (.add this x y))
  ([^Vector3f this x y z]
   (.add this x y z)))
```

## License

MIT License

Copyright (c) 2021 Ertuğrul Çetin

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
