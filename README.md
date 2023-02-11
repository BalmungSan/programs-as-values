# Programs as Values

Source code of the _"Programs as Values: The foundations of next-gen concurrent programming"_ tech talk.

## Links

+ **Slides**: https://www.dropbox.com/s/f1ry3kiau3hk0nb/Programs%20as%20Values%20%28Lambda%29.pptx?dl=0
+ **Talk recording** _(in Spanish)_: https://www.youtube.com/watch?v=iJ5jmMeAZwE&t=5652s&ab_channel=Josekell
+ **Programs as Values series** _(Fabio Labella)_: https://systemfw.org/archive.html
+ **The case for effect systems** _(Daniel Spiewak)_: https://www.youtube.com/watch?v=qgfCmQ-2tW0
+ **Streams - Your New Favorite Primitive** _(Ryan Peters)_: https://www.youtube.com/watch?v=BZ8O6T7Y1UE
+ **Functional Programming with Effects** _(Rob Norris)_: https://www.youtube.com/watch?v=30q6BkBv5MY&ab_channel=ScalaDaysConferences
+ **What is an Effect?** _(Adam Rosien)_: https://www.inner-product.com/posts/what-is-an-effect/
+ **Why FP** _(Luis Miguel Mejía Suárez)_: https://gist.github.com/BalmungSan/bdb163a080af54d3713e9e7c4a37ff51

## Extras

### Referential Transparency

Referential transparency is a property of expressions,
which dictates that you can always replace a variable with the expression it refers,
without altering in any way the behaviour of the program.<br>
In the same way, you can always give a name to any expression,
and use this new variable in all the places where the same expression was used;
and, again, the behaviour of the program must remain the same.

Let's see in practice what does that means:

```scala
val data = List(1, 2, 3)
val first = data.head
val result = first + first
println(result)
```

This little program will print `2` since `first` refers to the `head` of `data`; which is `1`<br>
Now, let's see what happens if we replace the `first` variable with its expression.

```scala
val data = List(1, 2, 3)
val result = data.head + data.head
println(result)
```

The result will be the same since `data.head` will always return `1`<br>
Thus, we can say that `List#head` is referentially transparent.

Let's now see and example that breaks the property:

```scala
val data = List(1, 2, 3).iterator
val first = data.next()
val result = first + first
println(result)
```

Again, the above program will print `2`;
because, `first` will evaluate to `data.next()`, which on its first call will return `1`<br>
But, this time the result will change if we replace `first` with the expression it refers to:

```scala
val data = List(1, 2, 3).iterator
val result = data.next() + data.next()
println(result)
```

In this case, the program will print `3`;
because, the first `data.next()` will return `1` but the second call will return `2`, so `result` will be `3`<br>
As such, we can say that `Iterator#next()` is NOT referentially transparent.

> **Note**: Expressions that satisfy this property will be called _"values"_.<br>
> Additionally, if a program is made up entirely of referentially transparent expressions _(values)_,
> then you may evaluate it using the _"substitution model"_.

### Composition

Composition is a property of systems,
where you can build complex systems by composing simpler ones.
In consequence, it also means that you can understand complex systems
by understating its parts and the way the compose.

> **Note**: Composition is not a binary attribute like Referential Transparency, but rather an spectrum;
> the more compositional our programs are then they will be easier to refactor.

### Monads

The (_in_)famous _"M"_ word,
Monads are a mechanism used to solve a fundamental problem that you will find
when using the _"Programs as Values"_ paradigm.<br>
To understand them, let's first understand the problem.

When our programs only manipulate plain values using functions _(which are also values)_,
it is very simple to compose those functions together into bigger ones.<br>
For example, if we had a function `f: A => B` and a function: `g: B => C`
then creating a function `h: A => C` is as simple as `h = a => g(f(a))`

However, what happens when we now have effectual values like `Option[A]` or `IO[A]`,
and effectual functions like `f: A => F[B]` and `g: B => F[C]`;
we can not longer use traditional function composition to create `h: A => F[C]`<br>
Although, we can do this:

```scala
val h: A => F[C] = { a: A =>
  f(a).flatMap(g)
}
```

Nevertheless, this requires the assumption that such `flatMap` function exists and has the type signature we want,
that is what Monads are, a Monad is just a triplet of a:
+ A type constructor _(`F[_]`)_
+ A `flatMap(fa: F[A])(f: A => F[B]): F[B]` implementation for that type constructor _(and also `pure(a: A): F[A]`)_
+ A proof that such implementation satisfies some laws

More importantly, such laws guarantee that such `flatMap` function somehow represents the concept of sequence.
Meaning that for `IO` `flatMap` always means do this and then do that, just like a `;` on imperative languages.
