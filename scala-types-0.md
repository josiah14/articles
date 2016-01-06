# Clean Up Your Error Handling Using Core Scala Algebraic Types

## Abstract

This article will go through the basics of using the algebraic datatypes native to the core Scala language through the `scala.util` and `scala` packages. By the end of this article, you should have the information necessary to use and understand code that uses all of the abstract datatypes in the core Scala language that are related to error handling.

## Prerequisites

There are some prerequisite aspects of the Scala language that you need to be familiar with before it makes sense to start talking about the higher kinded algebraic datatypes exposed by the core Scala libraries.  These Scala features are *pattern matching* and *collections*.

### Pattern Matching

Pattern matching in functional languages, such as Scala, allows us to assign names to data within datatypes and data structures according to where they appear in the data structure.  Pattern matching also allows you to match against not only the structure, but also the values and types within the structures themselves.  Let's look at a simple example:

```scala
val tuple0: Tuple2[Int, String] = (1, "grape")
val tuple1: Tuple2[Double, String] = (1.5, "orange")

def storeFruit(fruits: (Any, String)) = fruits match {
  // Match by value on "grape" and store the first value of the tuple in a variable called 'n'.
  case (n, "grape") => storeGrapes(n)

  // Match by type on the first value of the tuple and store it in the variable 'n', and ignore the second value in
  // the tuple.
  case (n: Double, _) => storeOranges(n)
}
```

The first instance of pattern matching happens in the function body of `storeFruit`. We first declare `fruits match {...}`.  This declaration states that we are pattern matching against the value of the `fruits` argument in the block of code contained within the following pair of curly braces.  In this example, we leverage the power of Scala's pattern maching to create a `case` statement.  In the first case, `n` is just a variable (you could also think of it as an alias) that holds the value of the first item in the tuple, so it will match for every value that the first tuple item could be.  However, for the second item of the tuple, we are matching on the `String` value `"grape"`.  So if the second item of the tuple is `"grape"`, we call the function `storeGrapes` and pass it `n`, which holds the value of the first item of the tuple.

However, if the second item is not the value `"grape"`, the we fall to the second `case` pattern.  In this second case, the match only succeeds if the first item in the tuple is of the type `Double`.  We use an underscore (`_`) for the second item because, in this case, we don't care what it is - we don't match on it and we don't use it.  However, we alias the first item of the tuple again to `n` because if the tuple 'matches', we call `storeOranges` and pass it `n`, which holds the value of the first item of the tuple. If we were to use a `_` here instead of aliasing the first tuple value to `n`, we would have no way of referencing that value to pass it to `storeOranges` besides to refence the original object, `fruits`, and call the `_1` method on it, such as `fruits._1`.  Neither way is wrong, but I think it's more readable to just alias the first value of the tuple to `n` using pattern matching.

Note that the Scala compiler will not check to see if you have matched against all of your edge cases.  In the above example, if we pass in a `Tuple2[Int, String]` where the second value is not the value `"grape"`, we will get a `scala.MatchError` at runtime because the value does not qualify for either of the declared cases. To prevent this error, you can add `case _ =>` as the last case, which will act as the default case if the other cases don't match and do nothing since there is no code to the right of the `=>`.  So the full case statement would look like the below, after the correction:

```scala
def storeFruit(fruits: (Any, String)) = fruits match {
  // Match by value on "grape" and store the first value of the tuple in a variable called 'n'.
  case (n, "grape") => storeGrapes(n)

  // Match by type on the first value of the tuple and store it in the variable 'n', and ignore the second value in
  // the tuple.
  case (n: Double, _) => storeOranges(n)

  // default case
  case _ =>
}
```

### Collections

I'm not actually going to talk in detail about the actual Scala Collections API in this section because you don't need to know much of it in order to start using algebraic datatypes.  However, there are some high-level functions which operate on collections that are beneficial to understand with respect to algebraic datatypes because they will implement many of the same functions.

The main functions we will go over are `map`, `flatMap`, `flatten`, `foreach`, `contains`, `exists`, `drop`, `head`, `filter`, `forall`, `fold`, `nonEmpty`, `orElse`, `getOrElse`, `zip`, and `unzip`.  We will talk about these functions in relation to `List`s because thats how they are most familiarly understood to programmers who are familiar with languages like Ruby, Python, and other languages that have some influence from function programming languages.

To start out, it helps to think about a `List` in Scala as a context that contains any number of values, rather than merely as a collection of values.  In general, any operation performed on a `container` value, such as a `List`, is immutable.  This means that the function and method calls will not mutate your data, but will rather pass back the result as a *new object*.  From this point of view, then, if I apply a function to a `List`, then it get's applied for every value contained by the context of that `List` object.  Let's explore this by starting out with the `map` function.

#### `map`
The `map` function takes a single argument function as an argument and applies it to every item in the `List` to create a *new `List`*.  You can either think of this as creating the new `List` by applying the function to every item in the `List`, or you could think of it as applying the function passed to `map` to the data in the context managed by the `List` object.  Either way, the only outcome that makes sense is that the result is a new `List` containing the results of applying `map`'s argument to every item in the `List`.

E.g. *(in the Scala REPL)*

```scala
scala> val xs: List[Int] = List(1, 2, 4)
scala> xs.map(n => n + 1)
res0: List[Int] = List(2, 3, 5)

scala> xs
res1: List[Int] = List(1, 2, 4)
```

It's important to note that the `map` function is intended for mathematically pure operations.  This means that it should not be used for operations that may create side effects, such as writing to disk, printing to the screen, or mutating a value in memory.  For those sorts of operations, you should, rather, use the `foreach` method, which will be discussed later.

#### `flatten`

`flatten` is pretty simple.  As you might guess, all it does is take a multidimensional list and decrease its dimensionality by one (so if you had a `List[List[List[Intt]]]`, calling flatten on it would give you a `List[List[Int]]` back.)

E.g. *(in the Scala REPL)*

```scala
scala> val xs List[List[Int]] = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
scala> xs.flatten
res0: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9)

scala> xs
res1: List[List[Int]] = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9)) 
```

**Exercise**

implement `flatMap` using `map` and `flatten`.

#### `flatMap`

`flatMap` is just like calling `flatten` after `map` - it `flatten`s out the result `List` in the event that this list is multidimensional (a.k.a the List contains other Lists).

E.g. *(in the Scala REPL)*

```scala
scala> val xs: List[Int] = List(1, 2, 4)
scala> xs.flatMap(n => List(n + 1))
res0: List[Int] = List(2, 3, 5)

scala> xs
res1: List[Int] = List(1, 2, 4)
```

As with `map`, `flatMap` should not be used for operations which may cause side effects.

#### `foreach`

`foreach` works exactly like `map` except it does **NOT** return a new `List`.  The reason the result `List` is not returned is because this function is intended for operations that cause side effects.  In other words, you use `foreach` when your intention is not to alter the `List` for a new result, but to do something stateful with the values of the list, such as printing them to the console, displaying them to a GUI, saving them to a database, writing them to a file, etc.  When you want to use the current `List` to generate a new result from some calculation, use `map`.

E.g. *(in the Scala REPL)*

```scala
scala> val xs List[Int] = List(1, 2, 3)
scala> val result = xs.foreach(n => println(n))
1
2
3
result: Unit = ()

scala> result 

scala> xs
res0: List[Int] = List(1, 2, 3)
```
You *could* do the above using `map`, but it's less efficient.  If you think about it a minute, you might understand the most obvious reason why: `map` will alway create a new result object, in this case, a `List`, whereas `foreach` never creates a result, it just performs an operation and then returns execution back to the caller. This means that when we use `foreach` the GC doesn't have to worry about cleaning up an unused object and we also save some memory since we never have to allocate those additional resources for instantiating a new `List` object.

#### `exists`

`exists` is like `contains`, except that it takes a lambda function that returns a `Boolean` to check against the list instead of a concrete value.  This allows us to check to see whether a `Collection` contains at least one value that satisfies a particular property, rather than needing to check for the existence of any possible value we might want.  `exists` returns `true` if the predicate returns `true` for at least one value in the `List`, and `false` otherwise.

E.g. *(in the Scala REPL)*
```scala
scala> val x: List[Int] = List(1, 2, 3)
scala> x.exists(n => n > 1)
res0: Boolean = true

scala> x.exists(n => n > 3)
res1: Boolean = false

scala> x
res2: List[Int] = List(1, 2, 3)
```

*A more interesting example*

```scala
scala> val x: List[Any] = List(1, 2, 3)
scala> x.exists({ case _: String => true; case _ => false })
res0: Boolean = false

scala> x.exists({ case _: Int => true; case _ => false })
res1: Boolean = true
```

#### `filter`

`filter` take a lambda function that returns a `Boolean` to serve as a predicate which filters out all of the values for which the predicate returns `false` in the result `List`.  Another way to say this is `filter` creates a new `List` which only contains the elements for which the predicate returned `true`.

E.g. *(in the Scala REPL)*

```scala
scala> val x: List[Int] = List(1, 2, 3, 4)
scala> x.filter(n => (n % 2) == 0)
res0: List[Int] = List(2, 4)

scala> x.filter(n => n < 0)
res1: List[Int] = List()

scala> x
res2: List[Int] = List(1, 2, 3, 4)
```

#### `forall`

`forall` takes a lambda predicate (a lambda function that returns a boolean) and returns `true` if all values in the `Collection` satisfy the predicate, and `false` otherwise.

E.g. *(in the Scala REPL)*

```scala
scala> val x: List[Int] = List(1, 2, 3, 4)
scala> val y: List[Any] = x
scala> x.forall(n => n > 1)
res0: Boolean = false

scala> x.forall(n => n > 0)
res1: Boolean = true

scala> x.forall({ case: _: String => true })
<console>:12: error: scrutinee is incompatible with pattern type;
 found   : String
 required: Int
       x.forall({ case _: String => true })

scala> y.forall({ case _: String => true })
scala.MatchError: 1 (of class java.lang.Integer)
  at $anonfun$1.apply(<console>:13)
  at $anonfun$1.apply(<console>:13)
  at scala.collection.LinearSeqOptimized$class.forall(LinearSeqOptimized.scala:83)
  at scala.collection.immutable.List.forall(List.scala:84)
  ... 43 elided

scala> y.forall({ case _: String => true; case _ =>  })
<console>:13: error: type mismatch;
 found   : Unit
 required: Boolean
       y.forall({ case _: String => true; case _ =>  })

scala> y.forall({ case_: String => true; case _ => false })
res2: Boolean = false

scala> (List(): List[Int]).forall(_ != null)
res3: Boolean = true
```

As you can see from the above, `forall` (and also `filter` and other related functions that take a lambda) can be quite handy, but you have to be careful to satisfy the type system when writing in your logic.  Most type errors should be caught by the Scala compiler, so if you have a good IDE, it should tell you when you're doing something that the Scala compiler doesn't like, but it should also be apparent from the error message above that you might not always get the most helpful error responses (take a look at the `scala.MatchError` when calling `forall` on `y` for the first time).  The best way to guard against running into these sorts of issues is to make sure you **1.** always provide a default case at the end to ensure all of your possible edge cases have a match, and **2.** make sure you don't test for cases that could never occur, such as in the last call of `forall` on `x` in the above example, where we try to match the contents of `x` on the `String` type when we already know via the type declaration on `x` that all of the items are `Int`s.  Scala's type system will catch such senseless attempts and actually prevent your code from compiling in these cases.

Another thing to note from the above is the last operation in the example set.  You see that operating on an empty `List` always results in `forall` evaluating to `true`.  If you think about this for a second, it makes sense.  If the `List` contains no elements, then as long as the predicate allows `forall` to satisfy the type signature of the `List`, all elements in the list will satisfy every predicate, because there are no elements to prove the predicate false.  In other words, all elements in an empty `List`  will satisfy every predicate possible precisely because there are no elements.

#### `fold`

Those coming from Python or Ruby will know `fold` under the name `reduce`.  What `fold` allows us to do is operate on each item in a `List` (or any container/contex, as we will see later) but keep track of the last value and append to it as we go until there are no more items left.  Note that the first value that `fold` takes (as a curried parameter) is the initial starting value for the operation.

Here is an illustration of `fold` on a `List`.

```scala

scala> List(1,2,3,4,5).fold(0)(_ + _)
res117: Int = 15

scala> List(1,2,3,4,5).fold(3)(_ + _)
res118: Int = 18
```

In the above example, you can see that the first `fold` operation starts at 0, so the end result is just 1 + 2 + 3 + 4 + 5 = 15.  But the second `fold` operation starts at 3, so the result is 3 + 1 + 2 + 3 + 4 + 5 = 18.  Note that with `fold`, your operation must be associative because the direction of the `fold`ing is not specified.  If you need to `fold` with a non-associative binary function, you will need to use `leftFold` or `rightFold`.  However, I will no be covering `leftFold` or `rightFold` in this article because the only abstract type we will be talking about that implements them is `Option`, but they each behave basically the same as `fold` on `Option`s since they are one value and the direction of the `fold`ing, therefore, doesn't matter.

#### `zip`

`zip` takes another `List` as a parameter and pairs up the elements of each of the `List`s by index.  In the event that one `List` is shorter than the other, the extra elements from the longer `List` will be excluded in the resulting `List`. The result `List` will be a `List` containing `Tuple2`s, with the first element of the tuple being the element from the first `List`, and the second element of the tuple being the element from the second `List`.

E.g. *(in the Scala RELP)*

```scala
scala> val x: List[Int] = List(1, 2, 3, 4)
scala> val y: List[String] = List("one", "two", "three")
scala> x.zip(y)
res0: List[(Int, String)] = List((1, "one"), (2, "two"), (3, "three"))

scala> x
res1: List[Int] = List(1, 2, 3, 4)
```

#### `unzip`

`unzip` undoes the effects of zip to provide a `Tuple2` of two `List`s.  The first list contains all of the first tuple values, and the second list contains all of the second tuple values.

E.g. *(in the Scala REPL)*

```scala
scala> val x: List[Int] = List(1, 2, 3, 4)
scala> val y: List[String] = List("one", "two", "three")
scala> x.zip(y).unzip
res0: (List[Int], List[String]) = (List(1, 2, 3), List("one", "two", "three"))
```

You can see from the above that we don't get the fourth element from `List` `x` back.  This is because the `zip` function drops that fourth element because there's nothing to pair it with in `List` `y`.  Since `zip` drops that value, `unzip` has no way to recover it, so it just provides a `List` of the three items it knows about for that `List`.

Note from the above examples for `unzip` and for `zip` that both of these functions are order preserving. This is because in zipping operations, most often, we care that the elements get paired up by their position in the `List`s.  This method is often used as a way of creating sets that have something similar to a key-value pairing relationship in their elements, or at least that the elements in each `List` being zipped have some sort of order or index related association.

#### `nonEmpty`

`nonEmpty` is just what it sounds like.  It returns `false` if the `List` is empty (has no elements), and `true` otherwise.

E.g. *(in the Scala REPL)*

```scala
scala> val x: List[Int] = List()
scala> x.nonEmpty
res0: Boolean = false

scala> 1 :: x
res1: List[Int] = List(1)

scala> x
res2: List[Int] = List()

scala> (1 :: x).nonEmpty
res3: Boolean = true
```

#### `orElse`

`orElse` will return a `PartialFunction` which takes an index argument and returns the item in the first encountered non-empty `List` at that index value.  The reason for this, possibly non-intuitive, behavior is that `orElse` is inherited from the `PartialFunction` trait through `AbstractSeq` which mixins `Seq` which extends `PartialFunction`.  So in order to operate on a `List`, Scala first converts the `List`s to the more generic type of `PartialFunction`, and then uses `orElse` to compose the two `PartialFunctions` together. You don't need to understand all of the details of this right now, but it's important to get a feel for this function because it is quite handy when working with many of Scala's high-kinded types.

E.g. *(in the Scala REPL)*

```scala
scala> var x: List[Int] = List()
scala> var pfun = x.orElse(List(3))
pfun: PartialFunction[Int,Int] = <function>

scala> pfun(0)
res0: Int = 3

scala> pfun = x.orElse(List())
pfun: PartialFunction[Int,Int] = <function1>

scala> pfun(0)
java.lang.IndexOutOfBoundsException: 0
  at scala.collection.LinearSeqOptimized$class.apply(LinearSeqOptimized.scala:65)
  at scala.collection.immutable.List.apply(List.scala:84)
  at scala.collection.immutable.List.apply(List.scala:84)
  at scala.PartialFunction$class.applyOrElse(PartialFunction.scala:123)
  at scala.collection.AbstractSeq.applyOrElse(Seq.scala:41)
  at scala.PartialFunction$OrElse.apply(PartialFunction.scala:167)
  at scala.Function1$class.apply$mcII$sp(Function1.scala:36)
  at scala.PartialFunction$OrElse.apply$mcII$sp(PartialFunction.scala:164)
  ... 43 elided

scala> pfun = List(3, 5).orElse(List(4))
pfun: PartialFunction[Int,Int] = <function1>

scala> pfun(1)
res1: Int = 5

scala> pfun = x.orElse("The list is empty!")
<console>:11: warning: a type was inferred to be `AnyVal`; this may indicate a programming error.
       var pfun = x orElse "The list is empty!"
                    ^
pfun: PartialFunction[Int,AnyVal] = <function>

scala> pfun(17)
res2: AnyVal = !
```

As you can see from the above, Scala doesn't protect you very much from errors such as choosing an invalid index, and you use some visibility (such as being able to get the `List` length) since everything gets converted to the more generic `PartialFunction` type.  This function may not be as useful for `Lists`, but for other types such as `Option` and `Try`, it can be very handy.

#### `getOrElse`

`getOrElse` is not implemented for `List`s, but I will make up a theoretical version of it to help introduce the concept here so that when it's introduced for other types you will have a conceptual reference point.

If `getOrElse` were implemented for the `List` type, I would imagine it as a function that gets the `head` of the `List` if the `List` is `nonEmpty`, or returns the argument provided to the function as a default value when the `List` is empty.  This particular concept of this function would work like the following:

*(in the Scala REPL)*

```scala
scala> val x: List[Int] = List(1, 2)
scala> val empty: List[Int] = List()
scala> x.getOrElse(5)
res0: Int = 1

scala> x.getOrElse(5)
res1: Int = 1

scala> empty.getOrElse(5)
res2: Int = 5
```

An alternative concept of this theoretical function for `Lists` that is less immutable and more stateful, but closer to the way a Prolog programmer would think, would be for `getOrElse` to return each of the `List`'s elements in sequence, and the default value once it gets to the end.

*(in the Scala REPL)*

```scala
scala> val x: List[Int] = List(1, 2)
scala> val empty: List[Int] = List()

scala> empty.getOrElse(5)
res2: Int = 5

scala> x.getOrElse(5)
res0: Int = 1

scala> x.getOrElse(5)
res1: Int = 2

scala> x.getOrElse(5)
res2: Int = 5

scala> x.getOrElse(5)
res3: Int = 5
```

Since this implementation is stateful, it is less in line with the Scala philosophy of preferring statelessness and immutability where possible, but mathematically, when considering a `List` as a nondeterministic value, this still makes some conceptual sense.

#### Comprehensions

List comprehensions in Scala might be a little bit different from what you are used to if you're coming from Haskell or Python.  I'll demonstrate comprehensions by giving the Haskell and Python syntax alongside the Scala syntax for the same thing, with the result provided for each.

##### Cartesian Product *(Examples in the REPL)*

##### Haskell

```haskell
Prelude> let as = [1, 2, 3]
Prelude> let bs = ["one", "two", "three"]
Prelude> let cartesianProduct xs ys = [(x, y) | x <- xs, y <- ys]
Prelude> cartesianProduct as bs
[(1,"one"),(1,"two"),(1,"three"),(2,"one"),(2,"two"),(2,"three"),(3,"one"),(3,"two"),(3,"three")]
```

###### Python
o
```python
>>> xs = [1, 2, 3]
>>> ys = ["one", "two", "three"]
>>> def cartesianProduct(xs, ys):
...   return [[x, y] for x in xs for y in ys]
... 
>>> cartesianProduct(xs, ys)
[[1, 'one'], [1, 'two'], [1, 'three'], [2, 'one'], [2, 'two'], [2, 'three'], [3, 'one'], [3, 'two'], [3, 'three']]
```

###### Scala

```scala
scala> val as: List[Int] = List(1, 2, 3)
as: List[Int] = List(1, 2, 3)

scala> val bs: List[Int] = List(4, 5, 6)
bs: List[Int] = List(4, 5, 6)

scala> val bs: List[String] = List("one", "two", "three")
bs: List[String] = List(one, two, three)

scala> def cartesianProduct(xs: List[Any], ys: List[Any]): List[Any] = for (x <- xs; y <- ys) yield (x, y)
cartesianProduct: (xs: List[Any], ys: List[Any])List[Any]

scala> cartesianProduct(as, bs)
res94: List[Any] = List((1,one), (1,two), (1,three), (2,one), (2,two), (2,three), (3,one), (3,two), (3,three))

scala> val bs: List[String] = List("one")
bs: List[String] = List(one)

scala> cartesianProduct(as, bs)
res95: List[Any] = List((1,one), (2,one), (3,one))
```

As you can see from the above, the Scala syntax can be thought of as sort of a mash-up between the Python and the Haskell syntaxes. Scala follows Python in leading with the `for` keyword to iterate over each element out of the `Collection`, but then follows `Haskell` more closely in using the `<-` operator to extract the current element from the collection and separating each extraction with a `,` rather than requiring a separate `for` expression for each `Collection` iteration being performed.

Let's look at another, slightly more interesting example before moving on to conditionals within a comprehension.

##### Scalar Product

###### Haskell

```haskell
Prelude> let as = [1, 2, 3]
Prelude> let bs = [4, 5, 6]
Prelude> let scalarProduct xs ys = [ x * y | (x, y) <- zip as bs ]
Prelude> scalarProduct as bs
[4,10,18]
```

###### Python

```python
>>> xs = [1, 2, 3]
>>> ys = [4, 5, 6]
>>> import itertools
>>> scalarProduct(xs, ys)
>>> def scalarProduct(xs, ys):
...   return [ z[0] * z[1] for z in itertools.izip(xs, ys) ]
... 
>>> scalarProduct(xs, ys)
[4, 10, 18]
```

###### Scala

```scala
scala> val as: List[Int] = List(1, 2, 3)
as: List[Int] = List(1, 2, 3)

scala> val bs: List[Int]  = List(4, 5, 6)
bs: List[Int] = List(4, 5, 6)

scala> def scalarProduct(xs: List[Int], ys: List[Int]): List[Int] = for ((x, y) <- xs.zip(ys)) yield x * y
scalarProduct: (xs: List[Int], ys: List[Int])List[Int]

scala> scalarProduct(as, bs)
res100: List[Int] = List(4, 10, 18)
```

As you can see from the above, like in Haskell, we can leverage Scala's pattern matching abilities to make working with the result of the `zip` function a little easier where in Python we had to rely on the extraction methods for Python `Iterator`s in order to get the values out for computing the product of each pair.

##### Pythagorean Triples using Conditionals

You can also filter the results of your comprehensions based on any conditional you can form using the variables within the comprehension expression by using the `if` keyword with a predicate.  In SQL, this would be like the `where` clause.  This behavior is the same as in Python when you use the `if` keyword in a comprehension.

Let's use the slightly contrived example of creating pythagorean triples from a list of numbers using List Comprehensions.

###### Haskell

```haskell
Prelude> let xs = [1..50]
Prelude> [ (a, b, c) | a <- xs, b <- drop (a - 1) xs, c <- drop (b - 1) xs, a^2 + b^2 == c^2 ] 
[(3,4,5),(5,12,13),(6,8,10),(7,24,25),(8,15,17),(9,12,15),(9,40,41),(10,24,26),(12,16,20),(12,35,37),(14,48,50),(15,20,25),(15,36,39),(16,30,34),(18,24,30),(20,21,29),(21,28,35),(24,32,40),(27,36,45),(30,40,50)]
```

###### Python

```python
>>> xs = range(1, 51)
>>> [ (a, b, c) for a in xs for b in xs[a - 1:] for c in xs[b - 1:] if a**2 + b**2 == c**2 ]
[(3, 4, 5), (5, 12, 13), (6, 8, 10), (7, 24, 25), (8, 15, 17), (9, 12, 15), (9, 40, 41), (10, 24, 26), (12, 16, 20), (12, 35, 37), (14, 48, 50), (15, 20, 25), (15, 36, 39), (16, 30, 34), (18, 24, 30), (20, 21, 29), (21, 28, 35), (24, 32, 40), (27, 36, 45), (30, 40, 50)]
```

###### Scala

```scala
scala> val xs: List[Int] = (1 until 51).toList
xs: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50)

scala> for (a <- xs; b <- xs.drop(a - 1); c <- xs.drop(b - 1) if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)) yield (a, b, c)
res9: List[(Int, Int, Int)] = List((3,4,5), (5,12,13), (6,8,10), (7,24,25), (8,15,17), (9,12,15), (9,40,41), (10,24,26), (12,16,20), (12,35,37), (14,48,50), (15,20,25), (15,36,39), (16,30,34), (18,24,30), (20,21,29), (21,28,35), (24,32,40), (27,36,45), (30,40,50))
```

You can see, here, that Scala looks a bit more like Python for the `Where` clause part of List Comprehensions in that it uses the `if` keyword to denote a conditional that will act as a filter on the results.

*Note that you could also write Scala comprehensions this (more readable) way, but the semicolons are always required inside the `for` parentheses:*

```scala
scala> for (
     | a <- xs;
     | b <- xs.drop(a - 1);
     | c <- xs.drop(b - 1)
     | if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
     | ) yield (a, b, c)
res10: List[(Int, Int, Int)] = List((3,4,5), (5,12,13), (6,8,10), (7,24,25), (8,15,17), (9,12,15), (9,40,41), (10,24,26), (12,16,20), (12,35,37), (14,48,50), (15,20,25), (15,36,39), (16,30,34), (18,24,30), (20,21,29), (21,28,35), (24,32,40), (27,36,45), (30,40,50))
```

## Higher-Kinded Error Handling Datatypes

Most, and possible all, higher-kinded datatyes in statically typed functional languages like Scala can be thought of as boxes that represent a context that may hold one or more values.  Types that can be thought of as a contextual box like this are called a `Functor` in Category Theory, and you can use this keyword to look up the specifics if you like.  However, I'm not going to talk at all about the scary Caterogical Theory behind these datatypes in this article.  Instead, I'm going to keep it simple, and show you how to use these higher-kinded Scala datatypes by example, instead.  I just thought I would mention that these things have a name, just in case you are the curious type that wants to just straight down into the rabbit hole.

Let's start out by remembering that I referred to the concept of `List`s as a collection of values held inside of the `List` context.  In this way, `List`s are a kind of box that holds zero, one, or many values.  The `List` datatype provides a series of functions for manipulating the data inside of a `List` context and even for extracting values out of the `List` context to be used in other computational contexts (such as printing them to the screen or storing them in a database in some manner, or even just using those values in the mathematically/functionally pure context of functions). Most of these operations we looked at in the last section, and the only other one I want to mention for the moment is the fact that a Scala `List` is, itself, just a function that takes an index `Int` and returns the value at that index from the `List`.

```scala
scala> List(1, 2, 3)(1)
res0: Int = 2
```

Most higher-kinded types provided natively by the core Scala libraries, and all of the types we will talk about in this article, also have methods that allow you to extract the values out of the context of type so the values can be used freely (although, not all higher-kinded types that could could encounter in Scala will necessarily allow you to do this).

### Introduction to Option, Try, and Either

There are three core types that, when used, are either always or most often used for error handling purposes.  These types each only hold at most on value at a time for each instance of the type.  The types we will cover are `Option`, `Try`, and `Either`.

#### Option

##### Introduction

The `Option` type is used to wrap a result from a function which might return a value, or might not return anything.  An instance of `Option` can be one of two values: `Some` or `None`.  If an `Option` object is the value `None`, as you might expect, that means the operation that produced the `Option` you are working with did not return a result; you actually got nothing back.  However, if your instance of `Option` is a `Some`, that `Some` value has inside of it "`Some`" value.  If your `Option` is of type `Option[Int]`, then your `Some` contains an integer.  If the type is `Option[List[String]]`, then your `Some` contains a `List` of `String`s. Your `Option` could even be the type `Option[Option[List[Int]]]`.  This instance is a little interesting, in that your value inside of the outer-most `Option` could either be a `Some` that contains a `List[Int]`, or `None`.  I'm sure by now you are beginning to get the picture, so let's look at some examples to help solidify this concept in your head.

##### Basic Examples

Throughout these examples, I'll introduce you to how you can use pattern matching to work with higher-kinded types like `Option`.  This is possibly the most common way to work with these types in scala because it's the most concise and easily understood way to look at a higher-kinded type's value and delegate accordingly.  I could try to explain this with words, but I think examples will do a better job.

```scala
scala> def makeOption(value: Any): Option[Any] = value match {
     |   case null => None
     |   case _ => Some(value)
     | }
makeOption: (value: Any)Option[Any]

scala> makeOption(null)
res19: Option[Any] = None

scala> makeOption(10)
res20: Option[Any] = Some(10)

scala> makeOption(List("apple", "pear", "grape", "pineapple", "strawberry"))
res21: Option[Any] = Some(List(apple, pear, grape, pineapple, strawberry))

scala> makeOption(makeOption(null))
res22: Option[Any] = Some(None)

scala> makeOption(makeOption(10))
res23: Option[Any] = Some(Some(10))
```

Looking at the above, you might think there is a way to improve our `makeOption` function.  If you think about it, if I have a `None` wrapped in a `Some`, I really still have nothing.  Therefore, it probably makes more sense to write `makeOption` like the following:

```scala
scala> def makeOption(value: Any): Option[Any] = value match {
     |   case null | None => None
     |   case _ => Some(value)
     | }
makeOption: (value: Any)Option[Any]

scala> makeOption(Some(List(1, 2, 3)))
res0: Option[Any] = Some(Some(List(1, 2, 3)))

scala> makeOption(None)
res1: Option[Any] = None

scala> makeOption(makeOption(makeOption(null)))
res2: Option[Any] = None
```

Looking at the above, you might also say, "Hey, wait! If nesting a `None` inside of a bunch of `Some`s doesn't make much sense, then couldn't the same be said of a regular Some(value)?", and you would be correct to say so.  A further improved `makeOption` might look like this:

```scala
scala> def makeOption(value: Any): Option[Any] = value match {
     |   case null | None => None
     |   case something @ Some(thing) => something
     |   case _ => Some(value)
     | }
makeOption: (value: Any)Option[Any]

scala> makeOption(makeOption(makeOption(null)))
res3: Option[Any] = None

scala> makeOption(makeOption(makeOption("a thing")))
res4: Option[Any] = Some(a thing)

scala> makeOption(Some(Some(10)))
res5: Option[Any] = Some(Some(10))

scala> makeOption(Some(List(1,2,3)))
res6: Option[Any] = Some(List(1, 2, 3))

scala> makeOption(Some(Some(None)))
res7: Option[Any] = Some(Some(None))
```

We could also write the above function as such to make a 100% logically equivallent function:

```scala
scala> def makeOption(value: Any): Option[Any] = value match {
     |   case null | None => None
     |   case Some(thing) => Some(thing)
     |   case x => Some(x)
     | }
makeOption: (value: Any)Option[Any]
```

*Node, in the above, using a name followed by the `@` symbol in a case statement allows you to give a name to the entire value to the right of the `@` for use on the right side of the `=>`. So, in this example, `something` ends up just being an alias for the whole of `Some(thing)`, which is really just `value`.  We could just use `value` instead of `something @ `, here, but I wanted to give a contrived example of another powerful Scala feature you can use in pattern matching.*

By now, you're probably seeing the pattern and getting the hang of this.  I'll leave it up to you to further improve `makeOption`.

**Exercise:** *Write an implementation for `makeOption` that doesn't return any Option values nested in Option values.  So, `makeOption(Some(Some(Some(10))))` should return `Some(10)`, and `makeOption(Some(Some(None)))` should return `None`, for example.*

*Hint for the above: you'll probably had to write an iterative or a recursive solution to complete the above exercise.  Notice anything particularly poor about the above code, and, likely, the code you wrote?  You lose most (or all) of your type information.  Keep in mind, this was an exercise to get you used to thinking and reasoning about abstract types, especially when they may be more complex and potentially nested.  But you typically wouldn't write this sort of code in the real world.*

##### When to Use Option and its Advantage Over Null

`Option` should be used when you have operations that might not return a value, but where you don't care to hold onto the exception or error message if one is produced. You can think of `Option` as a way of replacing `null` that is safer, because if you don't handle the `None` case, the Scala compiler will yell at you and force you to handle that case before letting you compile your code.  What this means is, if you use `Option` throughout your Scala code, you should rarely to never see a `NullPointerException` thrown during runtime.  All those years of wasted time and headaches on `NullPointerException`s gone!  This is what every Java programmer used to only dream about, now a dream come true.

As an example, let's consider consider the `get` function from the [scala-redis](https://github.com/debasishg/scala-redis) library.  If you try to `get` a value from a Redis key, there is a possibility that you won't `get` anything back because of reasons such as the key may not exist, the key may not have a value (or its value might be `null`).  No matter the case, the basic fact is, if you try to `get` the value of a Redis key, there exists the possibility that you may not `get` anything back.

Because of this possibility, [scala-redis](https://github.com/debasishg/scala-redis) implemented its `get` function to return an `Option`.  In the event that a value is returned, `get` returns a `Some` that holds the value it got back from Redis, and `None` if it wasn't able to get anything back.

Let's check out an example:

```scala
scala> import com.redis._
import com.redis._

scala> val redis = new RedisClient("localhost", 6379)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("test-key", "hello world!")
res0: Boolean = true

scala> def printRedisKeyValue(response: Option[String]): Unit = response match {
     |   case Some(value) => println(value)
     | }
<console>:13: warning: match may not be exhaustive.
It would fail on the following input: None
       def printRedisKeyValue(response: Option[String]): Unit = response match {
                                                                ^
printRedisKeyValue: (response: Option[String])Unit

scala> def printRedisKeyValue(response: Option[String]): Unit = response match {
     |   case Some(value) => println(value)
     |   case None => println("oops, there was no value at that key")
     | }
printRedisKeyValue: (response: Option[String])Unit

scala> printRedisKeyValue(redis.get("test-key"))
hello world!

scala> printRedisKeyValue(redis.get("not-a-key"))
oops, there was no value at that key
```

*Note that the non-exhaustive match warning above would be a compilation error if we were compiling such code in an application code base.*

#### Try

That last example for the scala-redis implementation of `get` brings us to our next datatype: `Try`.  `Try` is just what you might guess if you've programmed in languages like C++ and Java before that provide keywords for the `try ... catch ... finally` pattern of handling exceptions that might be thrown during runtime.  `Try` replaces the `try - catch - finally` keywords with a higher-kinded type that can be either a value of `Success` that holds the response from the successful operation, or a value of `Failure` that holds a `Throwable`, such as an `Exception`.

Someone coming straight from Java might try to use the scala-redis library as follows:

```scala
scala> var redisClient: RedisClient = null // We are already using null, so we know this is bad Scala practice
redisClient: com.redis.RedisClient = null

scala> try {
     |   redisClient = new RedisClient("localhost", 6379)
     | } catch {
     |   case _: RedisConnectionException => println("failed to connect to the Redis server")
     |   case _: Exception => println("failed to connect to Redis for unknown reasons")
     | }

scala> redisClient
res13: com.redis.RedisClient = localhost:6379

scala> if (redisClient != null) {
     |   try {
     |     redisClient.get("test-key") match {
     |       case Some(value) => println(value)
     |       case None => println("got nothing back")
     |     }
     |   } catch {
     |     case e: RedisConnectionException => println("failed to connect to the Redis server")
     |     case e: Exception => println("There was a communication error that occurred with Redis")
     |   }
     | }
There was a communication error that occurred with Redis
```

The underlying issue is that when we create a new Redis client, it might try to connect, but if there is no Redis server available at the provided host and port, it will throw a RuntimeException.  You can see that we have to violate one of the principals of Scala, immutability, in order to create our Redis client in a way that accounts for the possibility of `Exception`s.  This situation will occur most often when using Java libraries from Scala that could throw exceptions when instantiating the class (such as when creating an instance of a class that serves as a server client - if it cannot connect to the server, it will likely throw an exception).

Let's implement this the Scala way, using `Try`.

```scala
scala> import scala.util.Try
import scala.util.Try

scala> import scala.util.Success
import scala.util.Success

scala> import scala.util.Failure
import scala.util.Failure

scala> val redisClient: Try[RedisClient] = Try(new RedisClient("localhost", 6379))
redisClient: scala.util.Try[com.redis.RedisClient] = Success(localhost:6379)

scala> val valueAtKey: Try[[String]] = redisClient match {
     |   case Success(client) => Try(client.get("test-key"))
     |   case Failure(e) => Failure(e)
     | }
valueAtKey: scala.util.Try[[String]] = Failure(java.lang.RuntimeException: java.lang.StackOverflowError)

scala> valueAtKey match {
     |   case Success(value) => println(value)
     |   case Failure(_) => println("it failed, therefore, I am sad")
     | }
it failed, therefore, I am sad
```

You can see in the above code, first of all, how using the `Try` type flattens out our code and allows us to handle our errors as values on a case-by-case basis.  In the definition of `valueAtKey`, we return the result in the form of a `Try` from `client.get` if `redisClient` is a `Success` value, or if the `redisClient` is a `Failure` value, we just pass back the exception wrapped in a new `Failure` instance.  Someone clever might also see through this example how `Try` has the potential to flatten out code that would otherwise be solved by nested `try` blocks.

One such problem that often results in engineers writing nested `try` statements is when working with libraries that throw exceptions on class instantiation (and possibly in other scenarios). If I have to have two or more such class instances that work together to accomplish some task, it's really easy to accidentally miss freeing the resources of all clients in the event that an exception is thrown. This is because the programmer has to think about all potential resources that need to be freed in each `finally` block and is also responsible for checking that these resources aren't `null` or in an invalid state for freeing the resources before actually freeing them.  This sort of coding creates a brittle application with a lot of boilerplate error handling code.

Here is an example of this using an imaginary library for a service with a resource leak.

```scala
var configClient: ImaginaryConfigurationClient = null
try {
  var configClient = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
} catch {
  case e: Exception => System.err.println(e)
} // Oops, we forgot to use a finally to ensure that our configServer always closes its connections when we're done.
// If an exception is thrown in the above code block, the configServer never properly closes its connections.
// However, if we add a finally block, then we will close our configServer connection before we get a chance to use it.

if (configClient != null) {
  var imaginaryClient: ImaginaryServiceClient = null
  try {
    imaginaryClient = new ImaginaryServiceClient(configClient)
    imaginaryClient.doThings()
  } catch {
    case e: Exception => System.err.println("an error occurred when creating the ImaginaryServiceClient")
  } finally {
    if (imaginaryClient.connected) {
      imaginaryClient.destroy()
    }
    if (configClient.connected) {
      configClient.destroy()
    }
  }
}
```

One way to fix this is to nest your `try` blocks.

```scala
var configClient: ImaginaryConfigurationClient = null
try {
  configClient = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
  if (configClient != null) {
    var imaginaryClient: ImaginaryServiceClient = null
    try {
       imaginaryClient = new ImaginaryServiceClient(configClient)
       imaginaryClient.doThings()
    } catch {
      case e: Exception => System.err.println("an error occurred when creating the ImaginaryServiceClient")
    } finally {
      if (imaginaryClient.connected) {
        imaginaryClient.destroy()
      }
      if (configClient.connected) {
        configClient.destroy()
      }
    }
  }
} catch {
  case e: Exception => System.err.println(e)
} finally {
  if (configClient.connected) {
    configClient.destroy() 
  }
}
```

*Are your eyes bleeding, yet?*

In Scala, it's much easier to clean up these sorts of scenarios using `Try` because our results are wrapped by the `Try` context.  Let's take a look at how `Try` can be used to simplify the above examples of traditional `try - catch - finally` logic.

```scala
import scala.util.{Try, Success, Failure}

val configClient: Try[ImaginaryConfigurationClient] = Try(new ImaginaryConfigurationClient(appConfig.configurationServerAddress))

val imaginaryClient: Try[ImaginaryServiceClient] = configClient match {
  case Success(client) => Try(new ImaginaryServiceClient(client))
  case Failure(e) => Failure(e)
}

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configClient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

You can see from the above naive approach, first, that I had to write much less code, but also that the code has less nesting and is, therefore, more linear and easier to read.  `get` will try to get the value out of the `Success` if the `Try` is a `Success`, or will throw the `Exception` inside of the `Failure` if the `Try` is a `Failure`.  This is convenient, because it means we can use our `Try`s inside of a `Try()` and continue doing so in a linear fashion to describe the happy path without really needing to worry about possible `Failure`s until the end.  The code block above can be further simplified, given our current knowledge, using the `get` function to get the value out of the `Try` and attempt to do something with it.

```scala
import scala.util.{Try, Success, Failure}

val configClient: Try[ImaginaryConfigurationClient] = Try(new ImaginaryConfigurationClient(appConfig.configurationServerAddress))
val imaginaryClient: Try[ImaginaryServiceClient] = Try(new ImaginaryServiceClient(configClient.get))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configClient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

The above code will behave exactly the same as the previous code block on the surface.  The difference is minor, but in the previous example, the `Exception` gets directly wrapped into the new `Try` context through the `match` expression handles the `Failure` `case`, but in this example, when `get` encounters the `Failure`, it rethrows the `Exception` inside that `Failure`, which is then, of course, caught by the new `Try` context we are wrapping the `ImaginaryServiceClient` instantiation in.  However, even at this point, without understanding, yet, how to use comprehensions and `Collection`s functions on these datatypes, we can see that we have the power to drastically simplify code which needs to handle the possibility of `Throwable`s.

#### Either

`Either` is a lot like `Try`, in that it can be one of two values.  However, while `Try` is either a `Success` that holds the value returned from a function or operation when it successfully completes or a `Failure` that holds the `Throwable` which contains information about the error that occurred while trying to perform the operation, `Either` can be 'either' a `Right`, which, by convention, normally contains the value an operation returns for the expected, or successful, value (think, if you get a `Right`, things went 'right'), or else `Either` could be a `Left`, which normally holds some less-happy value such as an error (but it doesn't have to indicate an error).

##### When to Use Either Over Try

Some people out there in Google-land will tell you that `Either` is less preferred or inferior to the `Try` type and normally indicates a code-smell.  I'm not ready, yet, to draw that particular conclusion, but there is a distinction that needs to be made in purpose between `Try` and `Either` so that you know when to use each and how to design your APIs.

The main thing to realize is that `Try` is meant specifically for the purpose of handling `Exception`s.  Intelligently using `Either` vs. `Try`, therefore, requires some discipline around when you choose to throw `Exceptions` vs. just notify the application that some sort of error or issue that is expected to be common and non-critical arises.  For the latter case, `Either` is a better choice than `Try`.  The other main purpose for `Either` is when you have a function which may return one of two types, but neither type returned necessarily indicates an error.  Such cases as these are fairly uncommon, but they do arise from time to time, so it's good to know that the `Either` type is there if you ever run into such a scenario where you DO need a disjoint union as your return type.  Most of the time, though, it helps to think of `Either` as an alternative to `` that returns some information about why you have a `None` when a `None` occurs, rather than just being given back `None` with no explanation.

In the case of the scala-redis client's `get` method that we looked at previously, `` was fine because there's really only a few reasons for why you wouldn't get a value back from a key - either the key doesn't exist, or it does exist but it's value is `null`.  Either way, from a programming standpoint, it doesn't really matter most times what caused `get` to return `None`, what matters is that we know there is no value at that key.

A scenario for `Either` might be for handling HTTP responses.  `Right` value responses would be HTTP codes in the 100 and 200 ranges, and `Left` value responses would be HTTP codes in the 300 - 400 (although I could imagine arguments for a `Right` value'd 300s response where the redirect is expected), and perhaps 500 range codes producing an exception since they indicate unusual problems such as the Service being unavailable.

```scala
scala> import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.util.{Try, Success, Failure, Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@4ba1f84b

scala> def handleHttpResponse(httpResponse: Try[CloseableHttpResponse]): Try[Either[StatusLine, HttpEntity]] = httpResponse match {
     |   case Success(response) =>
     |     val statusLine = response.getStatusLine()
     |     statusLine.getStatusCode() match {
     |       case code if 100 until 227 contains code => Success(Right(response.getEntity()))
     |       case code if 300 until 500 contains code => Success(Left(statusLine))
     |       case code => Failure(new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase()))
     |     }
     |   case Failure(e) => Failure(e)
     | }
handleHttpResponse: (httpResponse: scala.util.Try[org.apache.http.client.methods.CloseableHttpResponse])scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val httpResponse: Try[Either[StatusLine, HttpEntity]] = handleHttpResponse(Try(httpClient.execute(new HttpGet("http://www.google.com"))))
Dec 11, 2015 1:35:21 PM org.apache.http.impl.client.DefaultHttpClient tryConnect
httpResponse: scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]] = Failure(java.net.NoRouteToHostException: No route to host)

scala> httpClient.getConnectionManager().shutdown()

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@4ba1f84b

scala> val httpResponse: Try[Either[StatusLine, HttpEntity]] = handleHttpResponse(Try(httpClient.execute(new HttpGet("http://localhost:3000"))))
Dec 11, 2015 1:39:41 PM org.apache.http.client.protocol.ResponseProcessCookies processCookies
httpResponse: scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]] = Success(Right(org.apache.http.conn.BasicManagedEntity@10a15612))
```

So, you can see in the above, anytime I get a response back, excluding 500 range HTTP status codes, I send back some data from the HTTP response.  The difference here, from what you might be traditionally used to with Java and other languages that don't have something akin to the `Either` type, is that `Either` allows us to indicate a kind of non-critical error and even to provide a different kind of data/type back when these less successful situations occur.  However, the above example still demonstrates that there is still an appropriate time to use `Try` and pass back an exception for errors that one would not expect to occur.  My attempt to hit "http://www.google.com" returns a `Failure` because I was behind a proxy which prevented the `httpClient` from being able to reach that address.  However, I had no problem talking to the web server I had running on "http://localhost:3000" at the time, so it returned a `Success[Right[HttpEntity]]` since that address on my web server returns a 200 status code upon success.  Had the "http://localhost:3000" route returned some other code that was outside of the 100-227 range, such as a 307 code for a Temporary Redirect, I would have gotten back a `Success[Left[StatusLine]]`, and I would have been able to recover the relevant information about why I got that code since the `StatusLine` type contains the "reason" sent with HTTP responses for why a certain error code was received, as well as the status code itself and even the protocol version information.  Had the response been a status code in the 500 range, I would have gotten back a `Failure[Exception]` with a message containing the status code and reason provided from the server for why the request failed.

Now, as you read this, you might think that this is all very interesting, but what happens when I want to get the value out of my `Either`?  I can't just `get` it like I can for `` and `Try` because it's not obvious that `Right` is necessarily the only truly successful value.  `Either` could be used to return a disjoint union where both possible response types are equally successful value. So, let's look at some examples for how to get the value out of our `Either`.

The most obvious way to get our `Either` values is to just pattern match on the `Either` type.  Building on the last code sample, let's get the value out of our `httpResponse` and do something with it. 

```scala
scala> val response = httpResponse.get
response: scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity] = Right(org.apache.http.conn.BasicManagedEntity@3b2de156)

scala> response match {
     |   case Right(httpEntity) => println(httpEntity.getContent().read())
     |   case Left(statusLine) => println(statusLine.getReasonPhrase())
     | }
60
```

This is a pretty silly example, but it shows that we can actually use pattern matching to do something with the value held inside of the `response` `Either` instance we have.  All the above does is print the first byte of data in the `httpEntity`'s content if it's a `Right`, and print the HTTP status reason if it's a `Left`.


However, what if we already knew it was a `Right`, or a `Left`, or were expecting it to be one or other do the degree that we would want an `Exception` to be thrown if it wasn't the `Either` value we were expecting?  Well, we can 'project' any `Either` instance as a `left` projection or a `right` projection.  They both work the same.  If I project my `Either` as a `Right` and it is a `Right`, then if I call `get` on that projection, I get back the value inside of the `Right`.  However, if it's a `Left`, it will throw an `Exception`.  The same goes for the `Left` projection, except `get` throws an `Exception` if it's a `Right`, and returns the value inside of the `Left` otherwise.

Let's look at an example.  Building on the previous code samples we have for `Either`, let's use projections to get the value out of our `response`, which is an instance of `Either`.

```scala
scala> response
res21: scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity] = Right(org.apache.http.conn.BasicManagedEntity@3b2de156)

scala> response.left.get
java.util.NoSuchElementException: Either.left.value on Right
  at scala.util.Either$LeftProjection.get(Either.scala:289)
  ... 43 elided

scala> response.right.get
res23: org.apache.http.HttpEntity = org.apache.http.conn.BasicManagedEntity@3b2de156

scala> response.right.get.getContent().read()
res24: Int = 33
```

In this example, you can see that `response` is a `Right[HttpEntity]`.  Therefore, when we project our response as a `Left` and try to `get` its value, it throws a `NoSuchElementException` and then tries to tell us that we can't get the value in `response` as a `Left` projection because `response` is actually `Right`.  Given this, I then said, "Oh, in that case, let me project `response` as a `Right` and try to get the value out of it that way!".  So, when I call `response.right.get`, I'm able to get my `HttpEntity` back.  And then, of course, I can do whatever I want with that `HttpEntity` that was returned to me.  Here, I just do the same thing as before and `read` out the next byte of content in my `HttpEntity`.

Most of the time with `Either`, pattern matching on the value will be the simplest way to work with your `Either` content because `Either` is most often used as a way to delegate execution to one function or the other based on the kind of data you got back.  However, projections are there for the rare instance where you are only interested in handling one of the `Either` values, and want to just ignore the content or throw an `Exception` in the event that you got the other `Either` value back.  However, be aware that if you are using `Either` to determine when you should throw `Exceptions`, you probably should be using `Try`, instead.

### Higher-kinded Types as Collections

Knowing the basics of the previous section which introduces ``, `Try`, and `Either` already gives us a lot of power.  However, there is still even more that we can do with these types than simply pattern match on them and used them as a type-safe way of ensuring proper error handling is written into the software.  As it turns out, all of those functions that were introduced way back in the Prerequisites > Collections section are also part of `Option`, `Try`, and `Either`.  These functions provide a way to interact with the value inside one of these three types without requiring the programmer to extract the value out of the context of the type.  There is some value in this because it allows the programmer to work with the type before handling the error path, or the unhappy path.  This way, the sad path and the happy path can be defined in full, but also separately from each other, so that it's easy to see what the purpose of the code is without all of the error handling noise cluttering up your happy path, and without the happy path obscuring your error handling strategy.  This might not make much sense to you, yet, but I'm confident that, as we go through the various collections functions and how to use them with these higher-kinded types, you will see how working with these types can simplify your code and make it easier to decouple your error paths from the rest of your code.

To introduce the collections functions and how they are used in the context of each type, my intention is to start with (what I perceive to be) the simpler functions, and then dig into the more complex (and often more handy) functions at the end, closing with comprehensions.  I'll also introduce using each function starting with the easiest type to work with, ``, then move on to `Try`, and then finish with `Either` (because `Either` is a sort of redheaded step-child among the three).

#### `map`

If you think of `map` in terms of a `List`, it applies a mutation (a function that doesn't cause side-effects) to all the items in the `List` without removing them from the `List` context so that the result is a new `List` of values representing the result of the mutation on each `List` item.  Map behaves just the same with our higher-kinded data types.  It will apply some mutation to the value wrapped by the context of our type (whether it's ``, `Try`, or `Either`), and give us back the result without taking us out of that context.  Let's make this clearer through some examples of using `map` on each of the types.

##### 

`` is what I think to be the most straightforward of all the other types we are looking at in this article.  You can think of `Option` as a special kind of `List` that holds at most one value, but might also be `None`.  If you `map` over an instance of `None`, you will just get a `None` back.  If you `map` over an instance of `Some`, `map` will apply the lambda argument (a.k.a. mutation) you supply it with to the value inside of the `Some` and give you back a new `Some` which holds the result of applying the mutation.  The advantage to `Option` being, as was already mentioned, that you are required to provide a case for the `None` possibility or else explicitly choose to ignore the type and try to get the value anyway (the latter being generally discouraged).

Let's `get` a value out of Redis and mutate its value to create a silly "hello world" application.

```scala
scala> import com.redis._
import com.redis._

scala> val redisClient: RedisClient = new RedisClient("localhost", 6379)
redisClient: com.redis.RedisClient = localhost:6379

scala> redisClient.set("hello-world-key", "Hello")
res59: Boolean = true

scala> redisClient.get("hello-world-key")
res60: [String] = Some(Hello)

scala> val hello = redisClient.get("hello-world-key")
hello: [String] = Some(Hello)

scala> val helloWorld = hello.map(_ + ", world!")
helloWorld: [String] = Some(Hello, world!)

scala> helloWorld match {
     | case Some(statement) => println(statement)
     | case None => System.err.println("The key, hello-world-key, did not contain a value upon access")
     | }
Hello, world!

scala> val oops = redisClient.get("not-a-key")
oops: [String] = None

scala> val oopsWorld = oops.map(_ + ", world!")
oopsWorld: [String] = None

scala> oopsWorld match {
     | case Some(statement) => println(statement)
     | case None => System.err.println("The key, not-a-key, did not contain a value upon access")
     | }
The key, not-a-key, did not contain a value upon access
```

If you are clever, you may recognize the advantage to using `map` as we did above.  `map` allows us to work with the value in our `` under the assumption that it's a successful`Some` value, and then check separately when we are done whether it's a `None` and handle that case on its own.  The following code sample from the REPL makes it more obvious how this is an advantage. 

```scala
scala> import com.redis._
import com.redis._

scala> val redisClient: redisClient = new RedisClient("localhost", 6379)
res75: com.redis.RedisClient = localhost:6379

scala> redisClient.rpush("queue", 1,2,3,4,5,6)
res79: [Long] = Some(6)

scala> val count: [Long] = redisClient.rpush("queue", 1, 2, 3, 4, 5, 6)
count: [Long] = Some(6)

scala> val items: List[[Int]] = count match {
     |   case Some(n) => (1 to n.toInt map { _ => redisClient.lpop("queue").map(num => num.toInt) }).toList
     |   case None => List()
     | }
items: List[[Int]] = List(Some(1), Some(2), Some(3), Some(4), Some(5), Some(6))

scala> val items: List[[Int]] = count match {
     |   case Some(n) => (1 to n.toInt map { _ => redisClient.lpop("queue").map(num => num.toInt) }).toList
     |   case None => List()
     | }
items: List[[Int]] = List(None, None, None, None, None, None)

scala> val count0: [Long] = None
count0: [Long] = None

scala> val items0: List[[Int]] = count0 match {
     |   case Some(n) => (1 to n.toInt map { _ => redisClient.lpop("queue").map(num => num.toInt) }).toList
     |   case None => List()
     | }
items0: List[[Int]] = List()

scala> items
res89: List[[Int]] = List(None, None, None, None, None, None)

scala> items.reduce((optnA, optnB) => (optnA, optnB) match {
     |   case (Some(a), Some(b)) => Some(a + b)
     |   case _ => None
     | })
res91: [Int] = None

scala> redisClient.rpush("queue", 1,2,3,4,5,6)
res92: [Long] = Some(6)

scala> val items: List[[Int]] = count match {
     |   case Some(n) => (1 to n.toInt map { _ => redisClient.lpop("queue").map(num => num.toInt) }).toList
     |   case None => List()
     | }
items: List[[Int]] = List(Some(1), Some(2), Some(3), Some(4), Some(5), Some(6))

scala> items.reduce((optnA, optnB) => (optnA, optnB) match {
     |   case (Some(a), Some(b)) => Some(a + b)
     |   case _ => None
     | })
res94: [Int] = Some(21)

scala> None :: items
res95: List[[Int]] = List(None, Some(1), Some(2), Some(3), Some(4), Some(5), Some(6))

scala> (None :: items).reduce((optnA, optnB) => (optnA, optnB) match {
     |   case (Some(a), Some(b)) => Some(a + b)
     |   case _ => None
     | })
res96: [Int] = None
```

You can see in the above code sample how we can just use `map` to convert our values returned from Redis from a `String` to an `Int` without leaving the `` context.  If Redis returned nothing, it's no big deal, because `map` just behaves like a noop in that case and leaves the `None` value as is.  We then deal with potential `None` values from Redis later in the `reduce` function by saying, hey, if I encounter a `None`, the value of the entire computation is `None`.  However, I could have also written my reduce as follows, and it would have been equally valid, from a theoretical standpoint.  Which implementation you would want depends on your particular needs at the time.

```scala
scala> (None :: items).reduce((optnA, optnB) => (optnA, optnB) match {
     |   case (Some(a), Some(b)) => Some(a + b)
     |   case (None, some @ Some(b)) => some
     |   case (some @ Some(a), None) => some
     |   case (None, None) => None
     | })
res98: [Int] = Some(21)
```

So, hopefully the above examples help you see the value of using `map` when it isn't necessarily advantageous, yet, to handle our `None` value(s) for our ``(s).

##### Try

For `Try`, `map` works essentially the same way as it does for ``, except that it will operates on `Success` values instead of `Some` values.  The idea is the same; if we are not ready to evaluate for exceptions, and just want to continue under the assumption that our `Try`s are `Success`es, we can use `map` and evaluate the results later.

Let's look at some examples of working with Redis in a way that handles potential `Exception`s.

```scala
scala> import scala.util.{Try, Success, Failure}
import scala.util.{Try, Success, Failure}

scala> import com.redis._
import com.redis._

scala> val redisClient: RedisClient = new RedisClient("localhost", 6379)
redisClient: com.redis.RedisClient = localhost:6379

scala> val count: Try[[Long]] = Try(redisClient.rpush("queue", 1,2,3,4,5,6))
count: scala.util.Try[[Long]] = Success(Some(6))

scala> val items: Try[List[[Int]]] = count.map(c => c match {
     |   case Some(n) => (1 to n.toInt map { _ => redisClient.lpop("queue").map(num => num.toInt) }).toList
     |   case None => List()
     | })
items: scala.util.Try[List[[Int]]] = Success(List(Some(1), Some(2), Some(3), Some(4), Some(5), Some(6)))

scala> val result: Try[[Int]] = items.map(None :: _).map(optns => optns.reduce((optnA, optnB) => (optnA, optnB) match {
     |   case (Some(a), Some(b)) => Some(a + b)
     |   case (None, some @ Some(b)) => some
     |   case (some @ Some(a), None) => some
     |   case _ => None
     | }))
result: scala.util.Try[[Int]] = Success(Some(21))

scala> result.map({
     |   case Some(n) => println(n)
     |   case None => println("All the Redis keys were empty.  Might indicate an issue, might not")
     | }).recover({
     |   case e: Exception => System.err.println(e)
     | })
21
res102: scala.util.Try[Unit] = Success(())
```

You can see from the above that, by mapping over the `Try` instances; `count`, `items`, and `result`; I was able to defer evaluation of the `Failure` case until the end.

You may note that I used `recover` here.  `recover` is a special function provided by `Try` that allows you to handle any `Failure` values, but then always return a `Success`.  It would also be good, here, to mention that using `map` to handle the `Success` cases allowed me to then create a separate `recover` block to handle the `Failure` cases.  When working with simpler workflows, I like to end my sequence with `recover` so that it's obvious where my `Exception` handling is occurring, but it's also a good tool for when you actually want to *`recover`* from an `Exception` and proceed as if the operation had generated a non-critical error.  You may find yourself using this more often when interfacing with Java APIs that overuse `Exception`s, as with Scala, most instances of errors you would want to `recover` from would be handled better by the `Either` type.  You could rewrite the above code sample as below, and it would be equally (or more) valid.

```scala
scala> result match {
     |   case Success(optn) => optn match {
     |     case Some(n) => println(n)
     |     case None => println("All the Redis keys were empty.  Might indicate an issue, might not")
     |   }
     |   case Failure(e) => System.err.println(e)
     | }
21
```

Notice in the above code sample, however, that the `match` function does not return a `Success` value, it just returns absolutely nothing (denoted in Scala by the `Unit` type).

Here is one last example of mapping over `Try`s, in case it hasn't sunk in, yet.  This doesn't present anything new, but is slightly less complicated and may be clearer for some people.

```scala
scala> import com.redis._
import com.redis._

scala> val r: Try[RedisClient] = Try(new RedisClient("localhost", 6379))
r: scala.util.Try[com.redis.RedisClient] = Success(localhost:6379)

scala> r.map(_.get("hello-key"))
res27: scala.util.Try[[String]] = Success(Some(hello-key))

scala> r.map(_ => throw new Exception("simulate a second failure"))
res30: scala.util.Try[Nothing] = Failure(java.lang.Exception: simulate failure)

scala> val r: Try[RedisClient] = Try(new RedisClient("www.google.com", 6379))
r: scala.util.Try[com.redis.RedisClient] = Failure(java.lang.RuntimeException: java.net.ConnectException: Connection refused)

scala> r.map(_ => throw new Exception("simulate a second failure"))
res32: scala.util.Try[Nothing] = Failure(java.lang.RuntimeException: java.net.ConnectException: Connection refused)
```

I think the above example makes it a bit clearer that `map` will only operate on a `Success`, so you will always retrieve the first thrown `Exception` when you decide to perform your error handling.  It's important to note that if you `map` over a `Failure` the lambda function you passed to `map` simply never gets called.

##### Either

`map`ping over an `Either` is a bit different, because it's not clear which projection, `Left` or `Right`, that you would want `map` to operate on just from the nature of the type itself.  Therefore, in order to map over an `Either` instance, you need to first declare which projection, `Left` vs. `Right`, you want `map` to apply to.  This will be the case for most to all of the Collections-like methods implemented for `Either`.  This is cited as a design problem by the Scala community, and may be a topic of a later Scala article if the topic proves worth the research based on my own programming experiences down the road.  The main disadvantage I see to using the `Either` type, at the moment, is if you want to chain your collections methods, such as calling `map(_ * 2).map(_ > 2)`, I actually have to specify the `RightProjection` for each one because each of the collections methods are defined for the Projections of `Either`, but the functions return the `Either` type.

Here's an example which illustrates this predicament:

```scala
scala> val disjointUnion: Either[String, Int] = Right(5)
disjointUnion: Either[String,Int] = Right(5)

scala> disjointUnion.right.map(_ * 2).map(_ > 5)
<console>:12: error: value map is not a member of Product with Serializable with scala.util.Either[String,Int]
       disjointUnion.right.map(_ * 2).map(_ > 5)
                                      ^

scala> disjointUnion.right.map(_ * 2).right.map(_ > 5)
res22: Product with Serializable with scala.util.Either[String,Boolean] = Right(true)
```

You should also notice from the above example that our operations can give us back an `Either` that holds different types (in this case, the `Right` projection holds a `Boolean` instead of an `Int` after the `map` operations).

Despite the mentioned problems, `map` still allows us to stay within our `Either` context, even if there's a bit more verbosity around it.  Note that `map`ping will not break if our `Either` instance happens to be the other projection.

To illustrate by building on the above example:

```scala
scala> disjointUnion.left.map(_ + " some more text")
res23: Product with Serializable with scala.util.Either[String,Int] = Right(5)
```

#### `flatten`

Flatten for abstract datatypes works the same as it does for `List`s.  If I have nested instances of the same datatype, `flatten` will resolve the nesting so that I only have to deal with a single wrapping of the context of whatever type I'm working in.

##### 

Let's start with the `` type, again.  If you have a `Some(Some(value))`, typically, that nested `Some` isn't giving any additional contextual information about the type that's of any real use to you.  Even if that value is a `None`, for the operation you want to perform, you probably only care that at the end of the nesting, there is a `None` there to help you delegate to the program what should be done next.

When using our scala-redis library, this could happen if we `get` a value from a Redis key and then use it to `get` a value from a different Redis key.

For example:

```scala
scala> import com.redis._
import com.redis._

scala> val redisClient: RedisClient = new RedisClient("localhost", 6379)
redisClient: com.redis.RedisClient = localhost:6379

scala> redisClient.set("hello-key", "world-key")
res0: Boolean = true

scala> redisClient.set("world-key", "hello world!")
res1: Boolean = true

scala> val worldKey: [String] = redisClient.get("hello-key")
worldKey: [String] = Some(world-key)

scala> val helloWorldValue: [Option[String]] = worldKey.map(redisClient.get(_))
helloWorldValue: [Option[String]] = Some(Some(hello world!))

scala> val flatHelloWorldValue: [String] = helloWorldValue.flatten
flatHelloWorldValue: [String] = Some(hello world!)

scala>

scala> val none: [String] = redisClient.get("not-a-key")
none: [String] = None

scala> val stillNone: [Option[String]] = none.map(redisClient.get(_))
stillNone: [Option[String]] = None

scala> stillNone.flatten
res2: [String] = None

scala>

scala> redisClient.del("world-key")
res7: [Long] = Some(1)

scala> redisClient.get("world-key")
res8: [String] = None

scala> val nestedNone: [Option[String]] = worldKey.map(redisClient.get(_))
nestedNone: [Option[String]] = Some(None)

scala> nestedNone.flatten
res9: [String] = None

scala> flatHelloWorldValue.foreach(println)
hello world!

scala> helloWorldValue.foreach(println)
Some(hello world!)

scala> helloWorldValue.foreach(_.foreach(println))
hello world!

scala> nestedNone.foreach(println)
None

scala> nestedNone.flatten.foreach(println)

scala>
```

The above shows the advantages of flattening out our nested abstract types.  It makes the code more concise and easier to understand and reason about, both for the initial implementer, and for the person the comes by later that needs to understand the code in order to work in it.  If we end up with nested structures, it's best to `flatten` them out to make the operations over the data contained within the datatype's context easier to reason about. Whether it's ``, `Try`, `Either`, or some other type, the concept is the same.

*You may be wondering, what if I have different types nested within each other, like an  inside of a Try?  That leads to a more advanced topic that we won't cover in this article called Monad Transformers.  Monad Transformers are not part of the core Scala API, but do exist in the ScalaZ library, and can be implemented manually using core Scala if needed.*

##### Try

`flatten` for `Try` works pretty much the same as it does for ``.  Scala does not allow nested Failures, so it's really a lot like `Option`, which only allows nested `Some`s, except in the case of `Try`, we have to manage nested `Success`s instead.

Here's a basic example of how `flatten`ning `Try`s works:

```scala
scala> import scala.util.{Try, Success, Failure}
import scala.util.{Try, Success, Failure}

scala> val tryMe: Try[String] = Failure(new Exception("oops"))
tryMe: scala.util.Try[String] = Failure(java.lang.Exception: oops)

scala> val tryTryMe: Try[Try[String]] = Failure(tryMe)
<console>:12: error: type mismatch;
 found   : scala.util.Try[String]
 required: Throwable
       val tryTryMe: Try[Try[String]] = Failure(tryMe)
                                                ^

scala> val tryTryMe: Try[Try[String]] = Success(tryMe)
tryTryMe: scala.util.Try[scala.util.Try[String]] = Success(Failure(java.lang.Exception: oops))

scala> tryTryMe.flatten
res0: scala.util.Try[String] = Failure(java.lang.Exception: oops)

scala> val tryTryMe0: Try[Try[String]] = Success(Success("hello world!"))
tryTryMe0: scala.util.Try[scala.util.Try[String]] = Success(Success(hello world!))

scala> tryTryMe0.flatten
res1: scala.util.Try[String] = Success(hello world!)

scala> tryTryMe0.flatten.map(println)
hello world!
res2: scala.util.Try[Unit] = Success(())
```

If you recall our example in the previous section on using `Try` to clean up the creation of services that depend on other services (section: *Introduction to Option, Try, and Either* > *Try*), you might already have an idea for how we could have written that code in a way that doesn't require us to rethrow exceptions.

Here is the original code, for convenience:

```scala
import scala.util.{Try, Success, Failure}

val configClient: Try[ImaginaryConfigurationClient] = Try(new ImaginaryConfigurationClient(appConfig.configurationServerAddress))
val imaginaryClient: Try[ImaginaryServiceClient] = Try(new ImaginaryServiceClient(configClient.get))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configClient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

If the `ImaginaryService` library had been written to return `Try` instances instead of throwing raw `Exceptions`, there is a good possibility that we could end up with nested `Try`s.  Using `flatten` can help us work with such code and keep it pretty clean and concise.

Here's how the code would look without `flatten`:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[Try[ImaginaryServiceClient]] = configClient.map(confClient => new ImaginaryServiceClient(confClient))

imaginaryClient match {
  case Success(imgClient) => imgClient match {
    case Success(client) => if (client.connected) {
      client.doThings()
      client.destroy()
    }
    case Failure(e) => System.err.println(e)
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

That code above is looking a bit ugly.  Let's clean it up with `flatten`.

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.map(confClient => new ImaginaryServiceClient(confClient)).flatten

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

You might be saying, "hey, we should use `map`, here!", but if you look closely, `doThings()` could cause side-effects, and `destroy()` definitely does cause side-effects.  I bet if you recall (or review) what `foreach` and `flatMap` do, however, you can probably guess how we are going to continue cleaning up this example code.

The last thing to note about `flatten` with `Try` is that, as with `Option`, the compiler will fail your build if you try to flatten a `Try` that is already as "flat" as it can be.  For `flatten` to be applicable, you need to have a `Try` nested in a `Try`, or else convert your datatypes so that the nesting can be resolved by the type system.

For example, you could convert your `Try` to an `Option` if your outer type is an `Option` to get the types to resolve.

```scala
scala> val value: Option[Try[String]] = Some(Success("value"))
value: Option[scala.util.Try[String]] = Some(Success(value))

scala> value.map(_.toOption).flatten
res37: Option[String] = Some(value)
```

##### Either

You may be surprized by this, but `Either` has no `flatten` function, nor does the `RightProjection` or `LeftProjection` of the `Either` type.  Let's take a look at an example to help illustrate the problem.

```scala

scala> val leftEither: Either[String, Int] = Left("three")
leftEither: Either[String,Int] = Left(three)

scala> val rightEither: Either[String, Int] = Right(3)
rightEither: Either[String,Int] = Right(3)

scala> val eitherLeftEither: Either[Either[String, Int], String] = Left(leftEither)
eitherLeftEither: Either[Either[String,Int],String] = Left(Left(three))

scala> val eitherRightEither: Either[Either[String, Int], String] = Left(rightEither)
eitherRightEither: Either[Either[String,Int],String] = Left(Right(3))

scala> val eitherEitherRight: Either[Either[String, Int], String] = Right("hello, this is weird")
eitherEitherRight: Either[Either[String,Int],String] = Right(hello, this is weird)

scala> eitherLeftEither.flatten
<console>:17: error: value flatten is not a member of Either[Either[String,Int],String]
       eitherLeftEither.flatten
                        ^

scala> eitherLeftEither.left.flatten
<console>:17: error: value flatten is not a member of scala.util.Either.LeftProjection[Either[String,Int],String]
       eitherLeftEither.left.flatten
                             ^
```

Do you see what the problem is?  `Either` is unbiased, so when I try to `flatten` a `LeftProjection[Either[String, Int], String]`, should I prefer the `Left` type get back an `Either[String, String]` or the `Right` type and get back a `Either[Int, String]`?  The compiler can't read your mind, so `flatten` is just not possible on `Either` types.

##### `rightJoin` and `leftJoin`

If you know which way you want to `flatten` your `Either`, you can use `rightJoin` and `leftJoin`.  I will only cover `leftJoin` since `rightJoin` works exactly the same, only it operates on the `RightProjection` instead of the `LeftProjection`.  I would have called these functions `rightFlatten` and `leftFlatten`, but I suppose that's nit-picking.

Here's an example of using `leftJoin` to flatten out a nested `Either`.

```scala
scala> val eitherLeftEither: Either[Either[String,Int],Int] = Left(Left("three"))
eitherLeftEither: Either[Either[String,Int],Int] = Left(Left(three))

scala> eitherLeftEither.joinLeft
res17: scala.util.Either[String,Int] = Left(three)

scala> val eitherRightEither : Either[Either[String,Int],Int] = Left(Right(3))
eitherRightEither: Either[Either[String,Int],Int] = Left(Right(3))

scala> eitherRightEither.joinLeft
res19: scala.util.Either[String,Int] = Right(3)

scala> val eitherRightEither : Either[Either[String,Int],String] = Left(Right(3))
eitherRightEither: Either[Either[String,Int],String] = Left(Right(3))

scala> eitherRightEither.joinLeft
<console>:16: error: Cannot prove that Either[String,Int] <:< scala.util.Either[C,String].
       eitherRightEither.joinLeft
                         ^

scala> val eitherRightEither : Either[Either[String,Int],Int] = Right(5)
eitherRightEither: Either[Either[String,Int],Int] = Right(5)

scala> eitherRightEither.joinLeft
res20: scala.util.Either[String,Int] = Right(5)
```

Note from the above that the types need to resolve properly so that if your outer `Either` happens to be the other projection from your join, that no information is lost.  In other words, to `flatten` your `Either`, your type can't be something like `Either[Either[String, Int], String]` because if you have a `Right` value and you do a `leftJoin`, the compiler can't know whether the `Right` type of the result should be an `Int` or a `String`.  If the inner-`Either` is a `Right`, then the result of `leftJoin` would be an `Either[String, Int]`, but if the outer `Either` is a `Right`, then the result of `leftJoin` would be an `Either[String, String]`.  Creating a version of `leftJoin` that could handle this would require leveraging dependent types, which is a more advanced topic and beyond the scope of this article.

#### `flatMap`

A lot of the time (probably even most of the time), you can avoid the need for using `flatten` by just using `flatMap` instead.  `flatMap` is just the same as calling `map(...).flatten`, so if the execution of your `map` lambda would case a nested data structure, you probably want to use `flatMap` (note, this is just the same is it is with the `List` type).

##### Option

Here is how someone might naively try to get a value out of Redis from a key that depends on the value from some other Redis key:

```scala
scala> val redisClient: RedisClient = new RedisClient("localhost", 6379)
redisClient: com.redis.RedisClient = localhost:6379

scala> redisClient.set("hello-key", "world-key")
res0: Boolean = true

scala> redisClient.set("world-key", "hello world!")
res1: Boolean = true

scala> redisClient.get("hello-key").map(key => redisClient.get(key))
res28: Option[Option[String]] = Some(Some(hello, world!))
```

You can see that the above results in a nested `Option` based on the type signature of the response.  We can clean that up by using `flatMap` instead of `map`.

In this example, I will demonstrate chaining `flatten` to the end of `map` before using `flatMap` to emphasize the equivalence of behavior between both implementations.

```scala
scala> redisClient.get("hello-key").map(key => redisClient.get(key)).flatten
res29: Option[String] = Some(hello, world!)

scala> redisClient.get("hello-key").flatMap(key => redisClient.get(key))
res30: Option[String] = Some(hello, world!)
```

But what if our nested value in the `Some` is a `None`?  Well, in that case, our result flattens out to be just `None`.  Below is an illustration:

```scala
scala> redisClient.del("world-key")
res31: Option[Long] = Some(1)

scala> redisClient.get("hello-key").map(key => redisClient.get(key))
res32: Option[Option[String]] = Some(None)

scala> redisClient.get("hello-key").flatMap(key => redisClient.get(key))
res33: Option[String] = None

scala> redisClient.get("hello-key").flatMap(key => redisClient.get(key)).flatMap(key => redisClient.del(key))
res36: Option[Long] = None
```

It is important to note that, like `flatten`, `flatMap` will cause a compile error if the type system determines that it was called on an operation that would not result in a nested `Option` structure.

Here is an illustration:

```scala
scala> redisClient.set("world-key", "Hello, world!")
res39: Boolean = true

scala> redisClient.get("hello-key").flatMap(key => redisClient.get(key))
res40: Option[String] = Some(Hello, world!)

scala> Try(redisClient.get("hello-key").flatMap(key => redisClient.get(key))).flatMap(println)
<console>:16: error: type mismatch;
 found   : () => Unit
 required: Option[String] => scala.util.Try[?]
       Try(redisClient.get("hello-key").flatMap(key => redisClient.get(key))).flatMap(println)
                                                                                      ^
```

The above example results in a type error because `println` does not return an `Option`, so the successful execution of the lambda in `flatMap` results in an `Option[Unit]`, which cannot be flattened any further, so `flatMap` doesn't make sense here.  In these cases, the type system is trying to tell you to use `map` instead.

##### Try

As with other examples, `Try` works almost exactly the same as `Option`, except `flatMap` operates on `Success` values instead of `Some` values.

Here's a short example to illustrate what mapping over a `Try` instance looks like.  We'll build on the previous `Try` example used for `flatten`:

Here's the original code:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.map(confClient => new ImaginaryServiceClient(confClient)).flatten

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

This can be shortened a little to look like the following:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.flatMap(confClient => new ImaginaryServiceClient(confClient))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

I'm also going to steal an example from the (Neophyte's Guide to Scala)[http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html] to illustrate an instance that you will probably encounter more often - needing to nest a `Try` constructor for handling an exception inside of the `map` lambda of another `Try` object.

Here's the naive implementation that results in a triple-nested `Try`:

```scala
import java.io.InputStream

def inputStreamForURL(url: String): Try[Try[Try[InputStream]]] = Try(new URL(url)).map(u =>
  Try(u.openConnection()).map(conn => Try(conn.getInputStream))
)
```

And the equivalent clean-up:

```scala
import java.io.InputStream

def inputStreamForURL(url: String): Try[InputStream] = Try(new URL(url)).flatMap(u =>
  Try(u.openConnection()).flatMap(conn => Try(conn.getInputStream))
)
```

`flatMap` on `Failure` instances works just like it does for `None` instances for `Option` types - it results in a noop and just gives you back the same `Failure`.  The same also goes for calling `flatMap` on `Try` instances that don't have a nested `Try` instance.  If you just have a `Success` that holds a normal value (something that is not a `Try`), the Scala type system will not let you use `flatMap` on operations that would not result in a nested `Try` structure.

##### Either

`flatMap`ping over an `Either`, like for `map` and the `join` functions, requires you to specify the projection you want to operate on, first (`Left` or `Right`).

Let's demonstrate this with a simple example:

```scala
scala> val eitherLeftEither: Either[Either[String,Int],Int] = Left(Left("hello"))
eitherLeftEither: Either[Either[String,Int],Int] = Left(Left(hello))

scala> eitherLeftEither.left.map(leftVal => leftVal.left.map(_ + ", world!")) // results in nested Either
res62: Product with Serializable with scala.util.Either[Product with Serializable with scala.util.Either[String,Int],Int] = Left(Left(hello, world!))

scala> eitherLeftEither.left.flatMap(leftVal => leftVal.left.map(_ + ", world!")) // results in unnested Either
res63: scala.util.Either[String,Int] = Left(hello, world!)

scala> val eitherLeftEither: Either[Either[String,Int],Int] = Left(Right(3))
eitherLeftEither: Either[Either[String,Int],Int] = Left(Right(3))

scala> eitherLeftEither.left.flatMap(leftVal => leftVal.left.map(_ + ", world!"))
res64: scala.util.Either[String,Int] = Right(3)

scala> eitherLeftEither.left.flatMap(leftVal => leftVal.right.map(_ + 5))
res65: scala.util.Either[String,Int] = Right(8)

scala> val eitherLeftEither: Either[Either[String,Int],Int] = Right(3)
eitherLeftEither: Either[Either[String,Int],Int] = Right(3)

scala> eitherLeftEither.right.flatMap(_ + 4)
<console>:16: error: type mismatch;
 found   : Int
 required: scala.util.Either[?,?]
       eitherLeftEither.right.flatMap(_ + 4)
                                        ^
```

Either is a little different from `Try` and `Option`, but the same basic rules apply; you need to be actually performing an operation that could result in a nested structure for `flatMap` to make sense.  In the last trial where we try to `flatMap` on the `right` permutation, since our `Right` value doesn't hold an `Either`, the compiler throws a type error.

#### `foreach`

`foreach` works just like `map`, except for it doesn't return anything.  This function is good for executing operations that cause side-effects over the value in the data type we are working with.  Since this is the only real difference between `foreach` and `map`, I'm not going to go into much detail in describing how it works, but, rather, I'm going to provide a simple example which isolates how it's used with each type, and then follow up with a more complex example for each which uses `map`, `flatMap`, and `foreach` to try to illustrate the difference in use-case for each.  When we get to the `Either` type, recall that we have to specify a permutation before `foreach` can be called.  It's also worth noting that making sure you use `foreach` in concurrent systems is especially important because `map` is not designed to handle side-effects.  If you try to use `map` for lambdas with side-effects, you may encounter some unpredictable results in your code.

##### Option

Here's a basic example of using `foreach` with `Option`.

```scala
scala> val opt: Option[String] = Some("hello")
opt: Option[String] = Some(hello)

scala> opt.map(_ + ", world!").foreach(println)
hello, world!

scala> opt.foreach { helloString =>
     |   println(helloString)
     |   println(helloString + ", Again!")
     | }
hello
hello, Again!

scala> None.foreach(println)

scala>
```

Again, not the lack of a return value on `foreach`.

Let's visit an example for working with Redis and see how it cleans up using `map`, `flatMap`, and `foreach`.

Original:

```scala
scala> import com.redis._
import com.redis._

scala> val redis = new RedisClient("localhost", 6379)
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("hello-key", "world-key")
res75: Boolean = true

scala> redis.set("world-key", "Hello")
res76: Boolean = true

scala> def printRedisKeyValue(response: Option[String]): Unit = response match {
     |   case Some(value) => println(value)
     |   case None => println("there was an error")
     | }
printRedisKeyValue: (response: Option[String])Unit

scala> printRedisKeyValue {
     |   redis.get("hello-key") match {
     |     case Some(worldKey) => redis.get(worldKey) match {
     |       case Some(helloStr) => Some(helloStr + ", world!")
     |       case None => None
     |     }
     |     case None => None
     |   }
     | }
Hello, world!
```

Using `map`, `flatMap`, and `foreach`:

```scala
scala> val redis = new RedisClient("localhost", 6379)
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("hello-key", "world-key")
res82: Boolean = true

scala> redis.set("world-key", "Hello")
res83: Boolean = true

scala> redis.get("hello-key").flatMap(redis.get(_)).map(_ + ", world!").foreach(println)
Hello, world!
```

It may just be me, but I'm able to understand what this last code sample is doing much quicker and easier than its predecessor.

##### Try

Here is a basic example for how `foreach` is used on an instance of `Try`:

```scala
scala> val aFailure: Try[String] = Failure(new Exception("oops"))
aFailure: scala.util.Try[String] = Failure(java.lang.Exception: oops)

scala> val aSuccess: Try[String] = Success("hello")
aSuccess: scala.util.Try[String] = Success(hello)

scala> aSuccess.map(_ + ", world!").foreach(println)
hello, world!

scala> aFailure.map(_ + ", world!").foreach(println)

scala> aSuccess.map(_ + ", world!").foreach { helloStr =>
     |   println(helloStr)
     |   println(helloStr + ", again!")
     | }
hello, world!
hello, world!, again!

scala>
```

For the more complex example, let's revisit our ImaginarServiceClient example.  At the start of talking about collections, we had this code:

```scala
import scala.util.{Try, Success, Failure}

val configClient: Try[ImaginaryConfigurationClient] = Try(new ImaginaryConfigurationClient(appConfig.configurationServerAddress))
val imaginaryClient: Try[ImaginaryServiceClient] = Try(new ImaginaryServiceClient(configClient.get))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configClient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

which we simplified with `map` and `flatten` to get:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.map(confClient => new ImaginaryServiceClient(confClient)).flatten

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

Which we then further simplified with `flatMap` to get:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.flatMap(confClient => new ImaginaryServiceClient(confClient))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

if (configCient.isSuccess && configClient.get.connected) configClient.get.destroy()
```

Now, let's take this even a step further by using `foreach`, `map`, and `flatMap`:

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)
val imaginaryClient: Try[ImaginaryServiceClient] = configClient.flatMap(confClient => new ImaginaryServiceClient(confClient))

imaginaryClient match {
  case Success(client) => if (client.connected) {
    client.doThings()
    client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

configClient.foreach(client => if (client.connected) client.destroy())
```

Notice how the code for cleaning up the configClient shortened up a bit.  We could have also used `foreach` instead of `match`ing on the `imaginaryClient`, but that makes handling the `Failure` case to print possible `Exception`s out to the logs more difficult and convoluted, so in this case, it's actually better to use `match`, there.

We could also write the above as follows and acheive the exact same result (though I think most would find this less readable, even though it's more concise):

```scala
val configClient: Try[ImaginaryConfigurationClient] = new ImaginaryConfigurationClient(appConfig.configurationServerAddress)

configClient.flatMap(confClient => new ImaginaryServiceClient(confClient)) match {
  case Success(client) => if (client.connected) {
      client.doThings()
      client.destroy()
  }
  case Failure(e) => System.err.println(e)
}

configClient.foreach(client => if (client.connected) client.destroy())
```

##### Either

Working with 'foreach' over an `Either` is just like `map` - you first need to declare which projection you want to work on since it's an unbiased type.  If the instance of `Either` you are working with happens to be the other projection from the one you choose to work on, `foreach` will just become a noop, so you don't have to worry about causing a runtime exception because you chose the 'wrong' projection on a `foreach` call.  This is by design, so that you can easily declare separate chains of operations for each projection using `foreach`.

Let's start with a simple example.

```scala
scala> val leftEither: Either[Int, String] = Left(5)
leftEither: Either[Int,String] = Left(5)

scala> val rightEither: Either[Int, String] = Right("hello")
rightEither: Either[Int,String] = Right(hello)

scala> leftEither.foreach(n => println(n * 3))
<console>:28: error: value foreach is not a member of Either[Int,String]
       leftEither.foreach(n => println(n * 3))
                  ^

scala> leftEither.left.foreach(n => println(n * 3))
15
res106: Any = ()

scala> leftEither.right.foreach(n => println(n * 3))
res107: Any = ()

scala> rightEither.foreach(str => println(str + ", world!"))
<console>:28: error: value foreach is not a member of Either[Int,String]
       rightEither.foreach(str => println(str + ", world!"))
                   ^

scala> rightEither.left.foreach(str => println(str + ", world!"))
res109: Any = ()

scala> rightEither.right.foreach(str => println(str + ", world!"))
hello, world!
res110: Any = ()

scala> def printEither(eitherInstance: Either[Int, String]): Unit = {
     |   eitherInstance.left.map(_ * 3).left.foreach(println).right.map(_ + ", world!").right.foreach(println)
     | }
<console>:27: error: value right is not a member of Any
         eitherInstance.left.map(_ * 3).left.foreach(println).right.map(_ + ", world!").right.foreach(println)
                                                              ^

scala> def printEither(eitherInstance: Either[Int, String]): Unit = {
     |   eitherInstance.left.map(_ * 3).left.foreach(println)
     |   eitherInstance.right.map(_ + ", world!").right.foreach(println)
     | }
printEither: (eitherInstance: Either[Int,String])Unit

scala> printEither(rightEither)
hello, world!

scala> printEither(leftEither)
15
```

Remember that each time we call a `Collection`s inspired method on an `Either`, that we need to specify the projection, even if we've already specified the projection for a previous method call.

The definition of `printEither` in the last example illustrates how easy it is to specify the behavior for each projection of the `Either` separately using the `Collection` methods like `map` and `foreach` without using `match`.  Using `match` is fine, but for chains of operations like the above, it's easier and more concise to use methods like `map` and `foreach`.  To illustrate, I'll provide an implementation of `printEither`, below, that uses `match`.

```scala
// exact equivalent of above.
def printEither(eitherInstance: Either[Int, String]): Unit = {
  eitherInstance match {
    case Left(value) => Left(value * 3)
    case Right(value) => Right(value + ", world!")
  } match {
    case Left(value) => println(value)
    case Right(value) => println(value)
  }
}

// A more concise version of the same, but still not as nice as using map and foreach
def printEither(eitherInstance: Either[Int, String]): Unit = {
  eitherInstance match {
    case Left(value) => println(value * 3)
    case Right(value) => println(value + ", world!")
  }
}
```

Now let's look at a more complex example which uses `map`, `foreach`, and `flatMap` by taking a look at a simplified version of our HTTP Client code.  We are going to exclude the use of `Try` in this example and just focus on using `Either` for the moment.

```scala
scala> import scala.util.{Either, Left, Right}
import scala.util.{Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@4ba1f84b

scala> def handleHttpResponse(httpResponse: CloseableHttpResponse): Either[StatusLine, HttpEntity] =
     |   httpResponse.getStatusLine().getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code => Left(httpResponse.getStatusLine())
     |   }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]

scala> val httpResponse: Either[StatusLine, HttpEntity] =
     |   handleHttpResponse(httpClient.execute(new HttpGet("http://dstconnect.dstcorp.net/display/dstconnect/Home")))
httpResponse: scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity] = Right(org.apache.http.conn.BasicManagedEntity@ec585ee)

scala> httpResponse match {
     |   case Right(httpEntity) => println(httpEntity.getContent().read())
     |   case Left(statusLine) => println(statusLine.getReasonPhrase())
     | }
60
```

Let's change this just slightly to provide an occassion for `flatMap` by including in here a temporary redirect.  In this code, we will handle the temporary redirect scenario by getting the redirect link out of the `StatusLine.getReasonPhrase()` in the `LeftProjection` of the `Either`.

```scala
scala> import scala.util.{Either, Left, Right}
import scala.util.{Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> var httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@4ba1f84b

scala> def handleHttpResponse(httpResponse: CloseableHttpResponse): Either[StatusLine, HttpEntity] =
     |   httpResponse.getStatusLine().getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code => Left(httpResponse.getStatusLine())
     |   }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]

scala> val httpResponse: Either[StatusLine, HttpEntity] =
     |   handleHttpResponse(httpClient.execute(new HttpGet("http://dstconnect.dstcorp.net/display/dstconnect/Home")))
httpResponse: scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity] = Right(org.apache.http.conn.BasicManagedEntity@ec585ee)

scala> def printResponse(httpResponse: Either[StatusLine, HttpEntity]): Unit = {
     |   case Right(httpEntity) => println(httpEntity.getContent().read())
     |   case Left(statusLine) => statusLine.getStatusCode() match {
     |     case 302 =>
     |       val redirectUrl: String = statusLine.getReasonPhrase().replace("Location: ", "")
     |       httpClient.getConnectionManager.shutdown()
     |       httpClient = new DefaultHttpClient()
     |       handleHttpResponse(httpClient.execute(new HttpGet(redirectUrl))) match {
     |         case res @ Left(statusLine) => statusLine.getStatusCode() match {
     |            case 302 => printResponse(res)
     |            case _ => println(statusLine.getReasonPhrase())
     |         }
     |         case res => printResponse(res)
     |       }
     |     case _ => println(statusLine.getReasonPhrase())
     |   }
     | }
printResponse: (httpResponse: Either[org.apache.http.StatusLine,org.apache.http.HttpEntity])Unit

scala> printResponse(httpResponse)
60
```

Now, let's refactor this a bit with the methods we've covered so far.  The imports won't change, so we'll leave them out for the rest of the examples in this section:

```scala
scala> var httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@4ba1f84b

scala> def handleHttpResponse(httpRespons: CloseableHttpResponse): Either[StatusLine, HttpEntity] =
     |   httpResponse.getStatusLine().getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code => Left(httpResponse.getStatusLine())
     | }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]

scala> val httpResponse: Either[StatusLine, HttpEntity] = 
     |   handleHttpResponse(httpClient.execute(new HttpGet("http://dstconnect.dstcorp.net/display/dstconnect/Home")))
httpResponse: scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity] = Right(org.apache.http.conn.BasicManagedEntity@ec585ee)

scala> def redirectTo(redirectUrl: String): Either[StatusLine, HttpEntity] = {
     |   httpClient.getConnectionManager.shutdown()
     |   httpClient = new DefaultHttpClient()
     |   handleHttpResponse(httpClient.execute(new HttpGet(redirectUrl)))
     | }

scala> def printResponse(httpResponse: Either[StatusLine, HttpEntity]): Unit = {
     |   httpResponse.right.map(_.getContent().read()).right.foreach(println)
     |   val redirectResponse = httpResponse.
     |     left.map(statusLine => (statusLine.getStatusCode(), statusLine.getReasonPhrase())).
     |     left.flatMap(pair => if (pair._1 == 302) redirectTo(pair._2.replace("Location: ", "") else httpResponse))
     |
     |   if (redirectResponse.isRight) printResponse(redirectResponse)
     |   redirectResponse.left.foreach(statusLine => statusLine.getStatusCode() match {
     |     case 302 => printResponse(redirectResponse)
     |     case _ => println(statusLine.getReasonPhrase()))
     |   }
     | }
```

You can probably already pick out ways to make this code even simpler (the recursion can be further utilized, for example, to simplify this code and eliminate the nested `Either` tyes all-together), but I'm going to leave it this way for now because this implementation lends itself to further refactoring using `filter`, which we'll get to later.

One disadvantage to using Collections methods with the `Either` type is that you lose some of the compile-time type-safety.  If you match on an `Either` and don't handle both projections, the compiler will throw an error.  However, if you use `isRight`, `foreach`, `map`, etc. on your projections, the compiler can't tell if you missed handling a projection, so it leaves you vulnerable to the possibility that you forget to handle one of the projections.  The advantage is that you don't have to leave the type in order to perform operations on it.  It seems more natural to me to use `isRight` and then pass `redirectResponse` back into `printResponse` recursively than it does to call `foreach` and start working with the value inside of the `Either` just to pass the full `Either` instance, `redirectResponse` back into `printResponse` when we handle the `302` case.

#### Mapping and Flattening with Multiple Types (Intermission)

Here, I'm going to pause a bit to give a more detailed example of mapping over abstract types and flattening them out when they become nested because mapping is a pretty important concept to get down.  The rest of the functions that we'll talk about until we get to Comprehensions are good to know about as helpers and utilities, but not as essential to working with abstract types as mapping is.  So let's move forward to a more complex example.

Now, remember this code from our intro to the `Either` type? Let's add in a call to Redis to get out the URL we want to hit so that we can see every type in action.  It's a bit of a contrived example, but I think it will still server the purpose of getting you used to seeing multiple types in action and interacting with one another within the same code.  This concept will become more important as we encounter some of the later functions, as they may return a different type that the one you started with.

```scala
scala> import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.util.{Try, Success, Failure, Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> import com.redis._
import com.redis._

scala> val redis: RedisClient = new RedisClient("localhost", 6379)
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home")
res152: Boolean = true

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@74e5f8ab

scala> def handleHttpResponse(httpResponse: Try[CloseableHttpResponse]): Try[Either[StatusLine, HttpEntity]] =
     |   httpResponse match {
     |     case Success(response) =>
     |       val statusLine = response.getStatusLine()
     |       statusLine.getStatusCode() match {
     |         case code if 100 until 227 contains code => Success(Right(response.getEntity()))
     |         case code if 300 until 500 contains code => Success(Left(statusLine))
     |         case code => Failure(new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase()))
     |       }
     |     case Failure(e) => Failure(e)
     |   }
handleHttpResponse: (httpResponse: scala.util.Try[org.apache.http.client.methods.CloseableHttpResponse])scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val HttpResponse: Option[Try[Either[StatusLine, HttpEntity]]] = redis.get("uri-requested") match {
     |   case Some(uri) => Some(handleHttpResponse(Try(httpClient.execute(new HttpGet(uri)))))
     |   case None => None
     | }
HttpResponse: Option[scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]] = Some(Success(Right(org.apache.http.conn.BasicManagedEntity@108697f4)))

scala> HttpResponse match {
     |   case Some(tryResponse) => tryResponse match {
     |     case Success(eitherResponse) => eitherResponse match {
     |       case Right(httpEntity) => println(httpEntity.getContent().read())
     |       case Left(statusLine) => println(statusLine.getReasonPhrase())
     |     }
     |     case Failure(e) => println(e)
     |   }
     |   case None => println("Error: no URI found at the Redis key attempted")
     | }
60
```

Let's see how we might, alternatively, write the above code using the Collections functions we've talked about so far that are available to our abstract types for error handling:

```scala
scala> val redis: RedisClient = new RedisClient("localhost", 6379)
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home")
res0: Boolean = true

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@1a97e2a9

scala> def handleHttpResponse(httpResponse: Try[CloseableHttpResponse]): Try[Either[StatusLine, HttpEntity]] =
     |   httpResponse map { response =>
     |     val statusLine = response.getStatusLine()
     |     statusLine.getStatusCode() match {
     |       case code if 100 until 227 contains code => Right(response.getEntity())
     |       case code if 300 until 500 contains code => Left(statusLine)
     |       case code => throw new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase())
     |     }
     |   }
handleHttpResponse: (httpResponse: scala.util.Try[org.apache.http.client.methods.CloseableHttpResponse])scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val httpResponse: Option[Try[Either[StatusLine, HttpEntity]]] =
    |    redis.get("uri-requested").map(uri => handleHttpResponse(Try(httpClient.execute(new HttpGet(uri)))))
HttpResponse: Option[scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]] = Some(Success(Right(org.apache.http.conn.BasicManagedEntity@397e3d8a)))

scala> Try(httpResponse.get).flatten match {
     |   case Success(response) =>
     |     response.right.map(_.getContent().read()).right.foreach(println)
     |     response.left.map(_.getReasonPhrase()).left.foreach(println)
     |   case Failure(e) => println(e)
     | })
60
```

In the above example, if we get a response code in [100, 126] we print out the first byte of the page content, if we get a response code in [300, 499] we print out the reason phrase that came with the response, if we get a response code in [500, *infinity*) we throw an exception with the code and reason phrase and print it to the console, and if we couldn't get a URL from Redis, all of those `map`s and `foreach` functions are skipped and we just print to the console the exception that `get` would throw in this scenario when called on the `None` `Option` that the Redis Client's `get` function returns when there is no value at a key (or the key doesn't exist).  Both examples are mostly equivalent, with the one difference being that in this last example we are throwing our `Exceptions` and allowing the `Try` function to catch them and wrap them in a `Failure` instead wrapping the `Exceptions` directly in a `Failure` as in the original code.

The main wins in the second example compared to the first is that we don't have to explicitly handle the `Failure` case just to re-wrap the exception into the new `Try` it returns in `handleHttpResponse`, we don't have to handle the `None` case just to pass back `None` again in `httpResponse`, and by converting our `httpResponse` `Option` into a `Try` in the last code block and calling `flatten` on it, we eliminated a level of nesting by needing to handle the `Option` cases explicitly.  We eliminated all of this explicit error handling while losing very little of the detail in our error reporting, which showcases really well the power of using these utility functions on our types to simplify our code.

I would challenge you, at this point, to take the above code and try other ways of using these functions to acheive the same result.

#### `exists`

First, it's important to be aware that there is no implementation of `exists` for `Either` or `Try`.  I'm starting out with this tidbit of information because you will not find a section for `Either` or `Try` for this function since they do not implement it as of this writing.  I'm not sure why `exists` has not been implemented on these types, but the fact remains that, as far as I could determine, it is not provided in the core Scala libraries for the `Either` or the `Try` types.

##### Option

What is common between `exists` for `List` and `Option` is that it takes a predicate.  If there is a value in the container that causes the predicate to return `true`, then `exists` returns `true`, but if there is no value that satisfies the predicate, `exists` will return `false`.  The difference is that, for `Option`, `exists` will return `false` if it's a `None` without exception.  Also, for `List`, there are many potential values that could satisfy the predicate and cause `exists` to return `true`, as only one value needs to satisfy it.  However, `Option` can only ever hold at most one value, so `exists` essentially tells you whether the value in the type instance satisfies the predicate you supplied to `exists`.

Here is a simple example:

```scala
scala> Some(5).exists(_ > 5)
res55: Boolean = false

scala> Some(5).exists(_ > 2)
res56: Boolean = true

scala> Some(5).exists(_.isInstanceOf[Int])
res58: Boolean = true

scala> Some(5).exists(_.isInstanceOf[String])
<console>:20: warning: fruitless type test: a value of type Int cannot also be a String (the underlying of String)
       Some(5).exists(_.isInstanceOf[String])
                                    ^
<console>:20: error: isInstanceOf cannot test if value types are references.
       Some(5).exists(_.isInstanceOf[String])
                        ^

scala> Some(5).exists(_.isInstanceOf[Double])
res60: Boolean = false

scala> val something: Option[Any] = Some(5)
something: Option[Any] = Some(5)

scala> something.exists { case n: String => true; case _ => false }
res43: Boolean = false

scala> something.exists { case n: Int => true; case _ => false }
res44: Boolean = true

scala> something.exists { case n: Int => n > 5; case _ => false }
res45: Boolean = false

scala> something.exists { case n: Int => n > 2; case _ => false }
res46: Boolean = true

scala> something.exists(_.isInstanceOf[Int])
res47: Boolean = true

scala> something.exists(_.isInstanceOf[String])
res49: Boolean = false

scala> something.exists(_ > 5)
<console>:20: error: value > is not a member of Any
       something.exists(_ > 5)
                          ^

scala> val something: Option[Any] = None
something: Option[Any] = None

scala> soemthing.exists(_ != null)
<console>:19: error: not found: value soemthing
       soemthing.exists(_ != null)
       ^

scala> something.exists(_ != null)
res52: Boolean = false

scala> something.exists(_ == null)
res53: Boolean = false

scala> something.exists(_ > 5)
<console>:20: error: value > is not a member of Any
       something.exists(_ > 5)
                          ^
```

As you can see in the above sample code, the type system will still prevent you from putting senseless predicates that are meaningless into you `exists` function, which is nice since these sorts of errors will be caught at compile time and the user will, therefore, never see them.  You can also see that you can provide partial functions as the lambda predicate (see the `case` expression examples) and Scala has no problem with figuring out how to run it as a predicate for `exists`, as long as it can satisfy the type system's requirements.

#### `filter`

As with other examples, `Option` behaves the most like a `List` for `filter`.  The other types (`Try` and `Either`) have some little idiosyncrasies, but also work mostly the same.  Let's look at some examples for each type.

##### Option

For `Option` types, if the predicate passed in as the argument to `filter` returns true, you get back the 'Some' value.  Otherwise, you get back a `None`.

Here is an illustration:

```scala
scala> Some(3).filter(_ > 3)
res121: Option[Int] = None

scala> Some(3).filter(_ > 2)
res122: Option[Int] = Some(3)

scala> (None: Option[Int]).filter(_ > 3)
res124: Option[Int] = None

scala> (None: Option[Int]).filter(_ != null)
res125: Option[Int] = None

scala> (None: Option[Int]).filter(_ == null)
res126: Option[Int] = None

scala> Some("hello, world!").filter(_.contains("world"))
res127: Option[String] = Some(hello, world!)

scala> Some("hello, world!").filter(_.contains("apple pie"))
res128: Option[String] = None
```

This is pretty much how `filter` works on a single element `List`, except instead of getting back an empty `List` when none of the values satisfy the predicate, I get back a `None`.

##### Try

`Try` works practically the same as `Option`.  If the predicate passed to `filter` evaluates to `true` for some value inside of a `Success` value of a `Try` instance, you get back an equivalent `Success` value.  If the value inside of a `Success` value doesn't causes the predicate to evaluate to `false`, you'll get back a `Failure` with a `java.util.NoSuchElementException`.  If it's a `Failure`, you always get back the unchanged `Failure` every time.

Here's an example:

```scala
scala> Success(5).filter(_ > 5)
res130: scala.util.Try[Int] = Failure(java.util.NoSuchElementException: Predicate does not hold for 5)

scala> Success(5).filter(_ > 4)
res131: scala.util.Try[Int] = Success(5)

scala> (Failure(new Exception("oops")): Try[Int]).filter(_ > 4)
res132: scala.util.Try[Int] = Failure(java.lang.Exception: oops)
```

##### Either

First, because the function you pass to `filter` needs to satisfy the type system, it can only be called off of the `RightProjection` or `LeftProjection` of `Either`, but this is nothing new.  We have seen this with other functions you can call on `Either`.

In addition to that difference, however, consider that `filter` for `Either` needs to be a little bit different because there isn't an empty constructor/value for `Either`.  For example, if my `filter` operation on the `RightProjection` of an `Either` that is a `Right` fails to satisfy its predicate for the value in the `Right`, what should it return?  It can't return a `Left` because, besides not know which value should go into that `Left`, doing so would also bias our `Either` to make `Left` values seem like error/empty values.

Scala's solution to this predicament is to have `filter` return a new `Either` wrapped in an `Option`.  If the predicate isn't satisfied either because the value in the projection we are operating on can't satisfy it, or because our instance of `Either` is the other projection, `filter` will give us back a `None`.  Otherwise, if the `filter` function's predicate is satisfied, `filter` will give us back a `Some` that holds the original value of our `Either` instance.  However, the type signature of the other projection will always change from whatever it was to start out, to `Nothing`.  For example, if I call `filter` on the `RightProjection` of an `Either[Int, String]`, I'll get back an `Option[Either[Nothing, String]]`.  I don't know why the language designers chose to implement `filter` this way, but, if you don't like it, you can always preserve your types by providing `filter` with an option type parameter which will become the `Left` type signature for the `Either` wrapped in an `Option` that `filter` gives back to you.

That's all a little confusing and hard to explain, so here's a good set of examples so that you can see how this function works on the `Either` type.

```scala
scala> val rightE: Either[String, Int] = Right(5)
rightE: scala.util.Either[String,Int] = Right(5)

scala> val leftE: Either[String, Int] = Left("Hello, world!")
leftE: scala.util.Either[String,Int] = Left(Hello, world!)

scala> rightE.left.filter(_.contains("world"))
res141: Option[scala.util.Either[String,Nothing]] = None

scala> rightE.left.filter[Int](_.contains("world"))
res142: Option[scala.util.Either[String,Int]] = None

scala> rightE.left.filter[List[Int]](_.contains("world"))
res143: Option[scala.util.Either[String,List[Int]]] = None

scala> leftE.left.filter(_.contains("world"))
res144: Option[scala.util.Either[String,Nothing]] = Some(Left(Hello, world!))

scala> leftE.left.filter(_.contains("dogs"))
res145: Option[scala.util.Either[String,Nothing]] = None

scala> leftE.left.filter[Int](_.contains("world"))
res146: Option[scala.util.Either[String,Int]] = Some(Left(Hello, world!))

scala> leftE.left.filter[Option[Double]](_.contains("world"))
res147: Option[scala.util.Either[String,Option[Double]]] = Some(Left(Hello, world!))

scala> rightE.right.filter { case 2 => false; case 5 => true }
res148: Option[scala.util.Either[Nothing,Int]] = Some(Right(5))

scala> rightE.right.filter[Double] { case 2 => false; case 5 => true }
res149: Option[scala.util.Either[Double,Int]] = Some(Right(5))

scala> rightE.right.filter[Int](_ == 5)
res150: Option[scala.util.Either[Int,Int]] = Some(Right(5))

scala> rightE.right.filter[String](_ == 5)
res151: Option[scala.util.Either[String,Int]] = Some(Right(5))

scala> leftE.right.filter[String](_ == 5)
res152: Option[scala.util.Either[String,Int]] = None

scala> leftE.right.filter[Double](_ == 5)
res153: Option[scala.util.Either[Double,Int]] = None

scala> leftE.right.filter(_ == 5)
res154: Option[scala.util.Either[Nothing,Int]] = None
```

#### `forall`

`forall` works on the abstract error handling types just like it does for the `List` type.  If there's is nothing in the successful bias of the type, or if there is nothing on the projection of `Either` being operated on, then `forall` always evaluates to `true`.  Otherwise, `forall` only evaluates to `false` if the value held by the type instance cases the predicate to evaluate to `false`.

##### Option

For `Option`, `forall` works just like it does for single element and empty `List`s where `None` is the empty `List` value, and `Some` is the single element `List` value.

```scala
scala> val something: Option[String] = Some("hello!")
something: Option[String] = Some(hello!)

scala> val nothing: Option[String] = None
nothing: Option[String] = None

scala> something.forall(_.contains("hello"))
res155: Boolean = true

scala> something.forall(_.contains("jello"))
res156: Boolean = false

scala> nothing.forall(_.contains("hello"))
res157: Boolean = true

scala> nothing
res158: Option[String] = None

scala> nothing.forall(_.contains("jello"))
res159: Boolean = true

scala> nothing.forall(_ == null)
res160: Boolean = true

scala> nothing.forall(_ != null)
res161: Boolean = true
```

There is really nothing tricky about `forall` when operating on an `Option`.  It works just the same as it does on `List`.

##### Try

`forall` has not been implemented on `Try` as of this writing, so for now, you can just forget about it for instances of `Try`.

##### Either

For `Either`, as with other functions on this type, `forall` is implemented only for the `LeftProjection` and `RightProjection` of this type, so to use `forall`, you first need to call `left` or `right` on your `Either` instance to get the projection you want.  Then, `forall` will return `true` if the `Either` is of the other projection, or else the result of running the predicate on the contained value if the `Either` instance is of the projection being operated on.

Below is an illustration:

```scala
scala> val leftE: Either[Int, String] = Left(5)
leftE: scala.util.Either[Int,String] = Left(5)

scala> val rightE: Either[Int, String] = Right("Hello!")
rightE: scala.util.Either[Int,String] = Right(Hello!)

scala> leftE.right.forall(_.contains("Hello"))
res166: Boolean = true

scala> leftE.right.forall(_.contains("Jello"))
res167: Boolean = true

scala> leftE.right.forall(_ == null)
res168: Boolean = true

scala> leftE.right.forall(_ != null)
res169: Boolean = true

scala> leftE.left.forall(_ > 4)
res170: Boolean = true

scala> leftE.left.forall(_ > 5)
res171: Boolean = false

scala> rightE.right.forall(_.contains("Hello"))
res172: Boolean = true

scala> rightE.right.forall(_.contains("Hello"))
res173: Boolean = true

scala> rightE.right.forall(_.contains("Jello"))
res174: Boolean = false

scala> rightE.left.forall(_ > 4)
res175: Boolean = true

scala> rightE.left.forall(_ > 5)
res176: Boolean = true

scala> rightE.left.forall(_ == null)
res177: Boolean = true

scala> rightE.left.forall(_ != null)
res178: Boolean = true
```

#### `fold`

##### Option

For `Option` types, `fold` returns the first curried parameter if it's a `None` or, otherwise, returns the result of the operation in the second parameter if it's a `Some`.

Here's a quick example:

```scala
scala> Option(3).fold(0)(_ + 4)
res120: Int = 7

scala> (None: Option[Int]).fold(0)(_ + 4)
res123: Int = 0
```

There's not much else to folding on `Option` types.  They pretty much just work like a single-element `List` where `None` is, more or less, equivalent to `Nil`.

To prime you for the following sections, consider this interesting property of `fold` on the `Option` type:

```scala
scala> Some(3).fold(None: Option[Int])(Some(_))
res0: Option[Int] = Some(3)

scala> (None: Option[Int]).fold(None: Option[Int])(Some(_))
res1: Option[Int] = None

scala> identity(Some(3))
res2: Option[Int] = Some(3)

scala> identity(None: Option[Int])
res3: Option[Int] = None
```

Do you see how we can define the `identity` function in terms of `fold` and the constructors for `Option`: `None` and `Some()`?  Let's build on this concept in the following sections for `Try` and `Either`.

##### Try

Before we even start, note that `fold` for `Try` was not implemented until Scala version 2.12.0-M1.  I'm running 2.11.7 on my machine, so the examples that I provide here will be short, simple, and untested (but should be accurate). ref: https://issues.scala-lang.org/browse/SI-8336

`fold` for `Either` and `Try` takes a little bit more understanding of what `fold` actually is in order to 'grok' it.  Remembering the last example for creating the identify function in terms of `fold` for `Option` types, consider folding over a `List` for a moment.  The way to understand how `fold` would work on an `Either` and `Try` is to consider how to turn `fold` into the identity function for `Lists`; in order to do this, you have to pass the `List` constructors into the `fold` operation to get back the same `List` as a result.  Observe:

```scala
scala> List(1,2,3).foldRight(Nil: List[Int])(_ :: _)
res92: List[Int] = List(1, 2, 3)

scala> identity(List(1,2,3))
res99: List[Int] = List(1, 2, 3)
```

This is just like `Option`, except the constructors for `Option` are `Some()` and `None`, with `None` being the empty value.

You may note that I had to use `foldRight` instead of the directionless `fold` operation for `List`. If you want to dig into fold directions, you can, but for now, just focus on the constructor `Nil` and the cons operator `::`.  If you aren't familiar with `::`, it is an infix operator that prepends the element on the left to the `List` on the right.  There is a law for `fold` which states that when the `fold` direction is right, when the empty value for the type being folded over (in the case of `List`, our empty value is `Nil`) is used as the starting value for the `fold` operation, and the type's constructor (in the case of `List`, our constructor is `::`) is used as the operation for performing the `fold`, this should give you the same thing back that you started with (also known as the identity function).  You can see this law satisfied for `fold` on the `List` type in the above example.  `foldRight(Nil: List[Int])(_ :: _)` is equivalent in it's end result to the `identity` function.

So, now, considering that the constructors for `Try` are `Success()` and `Failure()`, and knowing that both constructors require a value, we have to write `fold` to take two functions.  With `fold` taking two functions as it's argument, you can guess which one will be first; the function that operates on `Failure` instances needs to come first since `Try` is biased and `Failure` is considered the empty value for this type.  Below is an example to help illustrate how `fold` works on `Try` instances.

```scala
scala> val tryMe: Try[Int] = Success(5)
res0: Try[Int] = Success(5)

scala> val failMe: Try[Int] = Failure(new Exception("oops"))
res1: Try[Int] = Failure(java.lang.Exception: oops)

scala> tryMe.fold(Failure(_), Success(_))
res2: Try[Int] = Success(5)

scala> failMe.fold(Failure(_), Success(_))
res3: Try[Int] = Failure(java.lang.Exception: oops)
```

What this allows me to do is handle my `Failure` and `Success` cases in the same function on an instance of `Try`, so it's like a custom-tailored version of `get`, where I decide how I want to get back value if it's a `Success`, and how I want to handle the error case if it's a `Failure`.  Below are some more examples of using `fold` on a `Try`.

```scala
scala> tryMe.map(_ * 2).fold(System.err.println(_), println(_))
10

scala> failMe.map(_ * 2).fold(System.err.println(_), println(_))
oops

scala>
```

##### Either

Remembering the `Try` type, we can guess how the `Either` type is going to work. Considering that the constructors for `Either` are `Left()` and `Right()`, it follows that using those constructors in a `fold` operation on `Either` must be logically equivalent to calling the `identity` function on any instance of `Either`.  The way we can do that with `Either` is to just pass into `fold` two functions, the first one operating on `Left` values, and the second one operating on `Right` values.  That way, if we pass the `Either` constructors as our left and right functions respectively, we should get back an identical `Either` instance. Observe:

```scala
scala> val either: Either[Int, String] = Left(5)
either: scala.util.Either[Int,String] = Left(5)

scala> either.fold(Left(_), Right(_))
res102: Product with Serializable with scala.util.Either[Int,String] = Left(5)

scala> val either: Either[Int, String] = Right("hello")
either: scala.util.Either[Int,String] = Right(hello)

scala> either.fold(Left(_), Right(_))
res103: Product with Serializable with scala.util.Either[Int,String] = Right(hello)

scala> identity(either)
res104: scala.util.Either[Int, String] = Right(hello)
```

`Either` doesn't really have an 'empty' value because an `Either` can't be truly empty as it is unbiased, but even without empty, we can still kind of assume that `Left` would be our first function given the place indicated by its name 'left', but alse because `Left` is typically used to handle non-critical error cases.  Ultimately, there is still a way to create the `identity` function in terms of `fold` on the `Either` type, which is the ultimate point of this law.

This section wouldn't be complete without an example of using `fold` in a scenario where it doesn't end up being the identity function, so I've provided some examples below.

```scala
scala> val leftEither: Either[Int, String] = Left(-1)
leftEither: scala.util.Either[Int,String] = Left(-1)

scala> val rightEither: Either[Int, String] = Right("hello, world!")
rightEither: scala.util.Either[Int,String] = Right(hello, world!)

scala> leftEither.fold(System.err.println(_), println(_))
-1

scala> leftEither.fold(n => System.err.println(n - 5), println(_))
-6

scala> rightEither.fold(n => System.err.println(n - 5), println(_))
hello, world!

scala> leftEither.left.map(_ - 5).fold(System.err.println(_), println(_))
-6

scala> rightEither.left.map(_ - 5).fold(System.err.println(_), println(_))
hello, world!
```

#### `zip`

If you remember from the `List` type in the beginning, `zip` basically just takes two `List` instances and matches the elements together by index into tuples, with the elements from the first `List` that `zip` is called off of being the first element of each tuple, and the elements from the second `List`, passed into the `zip` function as an argument, being the second element of each tuple.


The same is true of our abstract types.  If I `zip` two `Options` together that are both a `Some`, I will get back a `List` with a tuple where the first element of the tuple is the value inside of the `Some` that `zip` was called off of, and the second element of the tuple being the value of the `Some` that was passed into the `zip` function.  For `Either` types, it works similarly, except you have to declare which projection you want to to work on, first. `zip` is only implemented on `Option`, as far as the abstract types we are talking about, so you will not see a section, here, for `Try` or `Either`.

##### Option

http://stackoverflow.com/questions/26345930/option-zip-returns-list-not-option

Before talking about `Option`, you might want to take a peak at the above link.  It's worth noting that the Documentation is still wrong for the `zip` function on `Options`.  Although the API docs say it will return an `Option`, it really returns an `Iterable`.  If you are zipping two `Options` together, you will get back a `List`.  Zipping other types with an `Option` might return different types that are subtypes of `Iterable`.  To keep it simple, we'll just look at zipping `Options` with other `Options` and with `List`s, but you should feel free to experiment in the REPL with other types.

```scala
scala> val hello: Option[String] = Some("hello")
hello: Option[String] = Some(hello)

scala> val world: Option[String] = Some(", world!")
world: Option[String] = Some(, world!)

scala> val nothing: Option[String] = None
nothing: Option[String] = None

scala> hello.zip(world)
res179: Iterable[(String, String)] = List((hello,, world!))

scala> hello.zip(world).map { case (x, y) => x + y }
res185: Iterable[String] = List(hello, world!)

scala> hello.zip(nothing)
res197: Iterable[(String, String)] = List()

scala> nothing.zip(hello)
res198: Iterable[(String, String)] = List()

scala> hello.zip(nothing).map { case (x, y) => x + y }
res199: Iterable[String] = List()

scala> nothing.zip(hello).map { case (x, y) => x + y }
res200: Iterable[String] = List()
```

#### `unzip`

`unzip` on abstract types works just the same as it does for `List`.  It will unpair your tuples and give you back a tuple of 2 `List`s, the first `List` being made up of the first tuple elements, and the second `List` being made up of the second tuple elements.  As with `zip`, `unzip` is not implement for `Try` or `Either` as of this writing, so we have ommitted those sections for this function.

Like `zip`, `unzip`ping `Option`s returns a pair of `Iterable`s (in most cases `List`s).  If you try to `unzip` on a `None`, you will just get back two empty `List` instances in the result tuple.  The reason for this is that, no matter the operation, the function's type signature must be satisfied.  To do this, `unzip` interprets a `None` as an `Option` that holds a tuple with no elements, which would result in an empty `List` for each element of the result tuple.  Note, however, that if you try to call `unzip` on an `Option` that doesn't hold a tuple, you will get a type error (your code won't compile), so this logic doesn't apply for non-tuple `Option`s.

```scala
scala> Some(("hello", ", world!")).unzip
res203: (Iterable[String], Iterable[String]) = (List(hello),List(, world!))

scala> (Some(1): Option[Int]).unzip
<console>:21: error: No implicit view available from Int => (A1, A2).
       (Some(1): Option[Int]).unzip
                              ^

scala> (None: Option[(Int, Int)]).unzip
res208: (Iterable[Int], Iterable[Int]) = (List(),List())

scala> (None: Option[Int]).unzip
<console>:21: error: No implicit view available from Int => (A1, A2).
       (None: Option[Int]).unzip
                           ^
```

Also worth noting is that the standard API also provides an `unzip3` if you have a 3 element tuple that you want to unzip.

```scala
scala> Some(("hello", ", ", "world!")).unzip3
res206: (Iterable[String], Iterable[String], Iterable[String]) = (List(hello),List(", "),List(world!))
```

##### Calling Option-only functions on Try and Either Instances (tangent discussion)

It's worth noting that for instance of `Try`, if you really need a function that's only on the `Option` type, you can call `toOption` on your `Try` instance and then chain the function you want to call off of that.  For a lot of these functions, converting to an `Option` is no big deal because, if you remember, these operations are immutable, so your original instance of `Try` will remain intact after calling `toOption`, as it returns to you a new `Option` instance without altering your existing `Try` instance.


Here's an example of how to use `toOption` to call `unzip` on a `Try` that is a `Success` containing a tuple.

```scala
scala> val successful: Try[(Int, Int)] = Success((1,2))
successful: scala.util.Try[(Int, Int)] = Success((1,2))

scala> successful.toOption.unzip
res210: (Iterable[Int], Iterable[Int]) = (List(1),List(2))

scala> successful
res211: scala.util.Try[(Int, Int)] = Success((1,2))
```

Note that if your `Try` instance is a `Failure`, `toOption` will give you a `None`.

```scala
scala> val failed: Try[(Int, Int)] = Failure(new Exception("oops"))
failed: scala.util.Try[(Int, Int)] = Failure(java.lang.Exception: oops)

scala> failed.toOption
res212: Option[(Int, Int)] = None

scala> failed.toOption.unzip
res213: (Iterable[Int], Iterable[Int]) = (List(),List())
```

The existence of `toOption` on `Try` instances and each of the `Either` projections is probably why a lot of the functions on `Option` are not directly implemented for `Try` and `Either`.  We skip most of these `Option`-only functions for this article because I want to focus more heavily on things that all of the error-handling abstract types have in common.  But you should certainly go through the API docs and try out some of these additional functions for yourself in the REPL and get used to them, as they can come in handy.

Speaking of `Either`, if you are wondering how `toOption` works on each of its projections, it works just the same as it does on `Try`, except that the case that causes `toOption` to evaluate to `None` is the case where the `Either` being operated on is of the other projection from the one declared.

Here is an example:

```scala
scala> leftE.left.toOption
res214: Option[Int] = Some(5)

scala> leftE.right.toOption
res215: Option[String] = None

scala> rightE.left.toOption
res216: Option[Int] = None

scala> rightE.right.toOption
res217: Option[String] = Some(hello)
```

#### `nonEmpty` and `isEmpty`

`nonEmpty` and `isEmpty` are another couple of those functions that are only available on `Option` instances, so if you want to use either one of these on `Try` or `Either`, you will have to convert your abstract type instance to an `Option` first.

These functions sound exactly like what they are - they just tell you whether you have a `Some` or a `None` `Option` instance.  If it's a `Some`, `nonEmpty` returns `true`, while `isEmpty` returns `false`.  If you have a `None`, it's the opposite; `nonEmpty` returns `false` while `isEmpty` returns `true`.

Here are some examples:

```scala
scala> something.isEmpty
res218: Boolean = false

scala> something.nonEmpty
res219: Boolean = true

scala> nothing.isEmpty
res220: Boolean = true

scala> nothing.nonEmpty
res221: Boolean = false
```

#### Recap - Using the Collections Functions Together Over Multiple Types and Instances

Remember our code for working with the Apache HTTP library's `HttpClient`?  Well, if you don't, here's the version of the code we left off with after talking about the functions for flattening out and mapping over our abstract types.

```scala
scala> import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.util.{Try, Success, Failure, Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> import com.redis._
import com.redis._

scala> val redis: RedisClient = new RedisClient("localhost", 6379)
redis: com.redis.RedisClient = localhost:6379

scala> redis.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home")
res0: Boolean = true

scala> val httpClient: DefaultHttpClient = new DefaultHttpClient()
httpClient: org.apache.http.impl.client.DefaultHttpClient = org.apache.http.impl.client.DefaultHttpClient@1a97e2a9

scala> def handleHttpResponse(httpResponse: Try[CloseableHttpResponse]): Try[Either[StatusLine, HttpEntity]] =
     |   httpResponse map { response =>
     |     val statusLine = response.getStatusLine()
     |     statusLine.getStatusCode() match {
     |       case code if 100 until 227 contains code => Right(response.getEntity())
     |       case code if 300 until 500 contains code => Left(statusLine)
     |       case code => throw new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase())
     |     }
     |   }
handleHttpResponse: (httpResponse: scala.util.Try[org.apache.http.client.methods.CloseableHttpResponse])scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val httpResponse: Option[Try[Either[StatusLine, HttpEntity]]] =
    |    redis.get("uri-requested").map(uri => handleHttpResponse(Try(httpClient.execute(new HttpGet(uri)))))
HttpResponse: Option[scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]] = Some(Success(Right(org.apache.http.conn.BasicManagedEntity@397e3d8a)))

scala> Try(httpResponse.get).flatten match {
     |   case Success(response) =>
     |     response.right.map(_.getContent().read()).right.foreach(println)
     |     response.left.map(_.getReasonPhrase()).left.foreach(println)
     |   case Failure(e) => println(e)
     | })
60
```

Let's investigate how we might use some of the functions we just looked at to further refactor this code.  First, notice that we are going to, now, wrap our `new RedisClient` instantiation call in a `Try`.  We know enough by now that we shouldn't be needing to reduce the complexity of this example.  We're also going to refactor `handleHttpResponse` so that it takes a raw `CloseableHttpResponse` instead of one wrapped in a `Try`.  We should be capable, at this point, of using our Collections functions so that we don't have much occasion for passing abstract types into our functions.

```scala
scala> val redis: Try[RedisClient] = Try(new RedisClient("localhost", 6379))
redis: scala.util.Try[com.redis.RedisClient] = Success(localhost:6379)

scala> redis.map(_.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home"))
res286: scala.util.Try[Boolean] = Success(true)

scala> val httpClient: Try[DefaultHttpClient] = { redis
     |   .map(_.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home"))
     |   .filter(_ == true)
     |   .map(_ => new DefaultHttpClient()) }
httpClient: scala.util.Try[org.apache.http.impl.client.DefaultHttpClient] = Success(org.apache.http.impl.client.DefaultHttpClient@5304d614)

scala> def handleHttpResponse(httpResponse: CloseableHttpResponse): Try[Either[StatusLine, HttpEntity]] = {
     |   val statusLine = httpResponse.getStatusLine()
     |   Try(statusLine.getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code if 300 until 500 contains code => Left(statusLine)
     |     case code => throw new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase())
     |   })
     | }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val httpResponse: Try[Option[Try[Either[StatusLine, HttpEntity]]]] = { redis.map(
     |   _.get("uri-requested").map(uri => httpClient.map(_.execute(new HttpGet(uri))).flatMap(handleHttpResponse(_)))
     | ) }
httpResponse: scala.util.Try[Option[scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]]] = Success(Some(Success(Right(org.apache.http.conn.BasicManagedEntity@1d358f17))))

scala> httpResponse.flatMap(
     |   _.fold(Failure(new Exception("No URI at Redis key")): Try[Either[StatusLine, HttpEntity]])(identity(_))
     |   .map(_.fold(statusLine => println(statusLine.getReasonPhrase()), httpEntity => println(httpEntity.getContent().read())))
     | ).fold(System.err.println(_), _ => Unit)
60

scala>
```

The above example isn't much more code, but it has better error handling and type-safety so that more errors are caught at compile-time.  The first thing to note is that we wrap our `RedisClient` instance in a `Try` because, if there is no Redis server available at the provided address and port, it could throw an `Exception`.  Then, we find that most of what we want to do with the `RedisClient` instance can be done through mapping, but we can now use `filter` to check if we successfully set the URI in Redis.  If not, `filter` will return a `Failure` with an appropriate exception, otherwise, the subsequent `map` operation creates our `DefaultHttpClient`.  What this means, so far, is that if we fail to set the URI properly in Redis, the rest of the code up until the final `fold` command at the very end won't get executed, and the only thing that will run is the first argument to the very last `fold` command, namely `System.err.println(_)`.

Now, the code for `handleHttpResponse` and for creating the `httpResponse` object is only refactored a little bit to make better use of our `map` and `flatMap` functions.  But there is nothing new here in terms of content that we didn't see from the last time we looked at this code.  But take a look at how we use `httpResponse`; `flatMap` allows us to take the internal `fold` instruction that converts our `Option` to a `Try` and `flatten` it out to a `Try[Either[StatusLine, HttpEntity]]` instead of a `Try[Try[Either[StatusLine, HttpEntity]]]`.  This allows us to handle all of our error logging/printing for this entire section of code in the final `fold` that trails the closing parenthesis of the `flatMap` function.  This is pretty neat - using mapping, flattening, and folding operations, we've been able to declare how to work with the values in our datatypes assuming they are the successful values we expect, with the one exception of the `fold` on our nested `Option` at the end where we provide a custom `Exception` for the event where we failed to retrieve the `URI` from the Redis server.

The only other `fold` operation is the one inside of the `map` operation, inside of the `httpResponse.flatMap` function call.  This `fold` operates on our `Either` instance, which holds our actual response from the `DefaultHttpClient` after making its GET request to the URI from the Redis key.  In this `fold` operation, we declare what to do with both the `Left` and `Right` cases of our httpResponse `Either` using a simple lambda expression for each.  It is concise enough that this still all fits on one line.

Of course, if we were writing our code outside of the REPL, we might want to move some of the error handling up to some of our intermediate result objects to help cut down on the crazy type signatures we ended up with (especially the one for the `httpResponse` object that was as `Try[Option[Try[Either[StatusLine, HttpEntity]]]]`).  But I think this current example goes far enough to demonstrate the power and usefulness of these Collections functions on our abstract data types.

#### `orElse`

`orElse` and the following functions are some of the most interesting and useful functions on these types.  The functions before were kind of just primer for these next two functions (`orElse` and `getOrElse`), and for the last capability, which is Comprehensions.

`orElse` returns the type instance as it is if it's of the positively biased value or of the projection being operated on (in the case of `Either`), or the default value passed in as its argument otherwise.  Let's take a look at how this works with each type.

##### Option

For `Option` types, `orElse` just returns the same `Option` if it is a `Some`, or the value passed in as its argument if it's a `None`.

For example:

```scala
scala> val something: Option[Int] = Some(1)
something: Option[Int] = Some(1)

scala> val nothing: Option[Int] = None
nothing: Option[Int] = None

scala> something.orElse(Some(10))
res222: Option[Int] = Some(1)

scala> nothing.orElse(Some(10))
res223: Option[Int] = Some(10)

scala> nothing.orElse(10)
<console>:22: error: type mismatch;
 found   : Int(10)
 required: Option[?]
       nothing.orElse(10)
                      ^

scala> something.orElse(10)
<console>:22: error: type mismatch;
 found   : Int(10)
 required: Option[?]
       something.orElse(10)
                        ^
scala> something.orElse(Some("hello"))
<console>:22: warning: a type was inferred to be `Any`; this may indicate a programming error.
       something.orElse(Some("hello"))
                            ^
res226: Option[Any] = Some(1)
```

Note that the type system requires your 'else' value to be of the same type being operated on itself (in this case, an `Option`), but it's not as picky about which type the value inside of the `Option` should be.  It will still throw a warning if your else value would change the type in the `Option`, but the type system will still resolve it by giving you back an `Option[Any]` to satisfy the type signature of the operation.

The powerful thing about this is that it allow you to continue chaining operations on your `Option` instances while explicitly handling your error case, without even leaving the context of the `Option` type.  You can think of this as a way of transforming the `None` case without needing all the verbosity of a `match` expression.

```scala
scala> val something: Option[Int] = Some(1)
something: Option[Int] = Some(1)

scala> val nothing: Option[Int] = None
nothing: Option[Int] = None

scala> something.map(_ + 2).map(_ % 3).orElse(Some(-1))
res227: Option[Int] = Some(0)

scala> nothing.map(_ + 2).map(_ % 3).orElse(Some(-1))
res228: Option[Int] = Some(-1)
```

##### Try

`Try` works pretty much the same as `Option` for `orElse`.  If the value is a `Success`, you get back the same `Success` value, if it's a `Failure`, you get back the `Success` you passed in as the argument to `orElse`.

```scala
scala> val successful: Try[String] = Success("hello")
successful: scala.util.Try[String] = Success(hello)

scala> val failed: Try[String] = Failure(new Exception("oops"))
failed: scala.util.Try[String] = Failure(java.lang.Exception: oops)

scala> successful.map(_ + ", ").map(_ + "world!").orElse(Success("good-bye!"))
res229: scala.util.Try[String] = Success(hello, world!)

scala> failed.map(_ + ", ").map(_ + "world!").orElse(Success("good-bye!"))
res230: scala.util.Try[String] = Success(good-bye!)
```

##### Either

Interestingly, `Either`, nor its `LeftProjection` nor `RightProjection`, implement `orElse`.  I'm not sure what the reason for this is, but since it's not implemented in the current API, we won't be digging into it, here.

#### `getOrElse`

`getOrElse` is a lot like `orElse`, except it takes you out of the type and allows you to declare a default value if there's nothing in the type (because it's either a `None`, or a `Failure`, or the other projection of the type).

As a first illustration, let's see how `getOrElse` works on `Option`.

##### Option

For `Option`, `getOrElse` will return the value inside the `Some` if it's a `Some` and, otherwise, it will return the value you pass in as its argument if it's a `None`.

Here's some examples to help you grasp how this function works on this type:

```scala
scala> val something: Option[Int] = Some(1)
something: Option[Int] = Some(1)

scala> val nothing: Option[Int] = None
nothing: Option[Int] = None

scala> something.getOrElse(10)
res232: Int = 1

scala> nothing.getOrElse(10)
res233: Int = 10

scala> something.map(_ * 3).map(_ % 2).getOrElse(-1)
res243: Int = 1

scala> nothing.map(_ * 3).map(_ % 2).getOrElse(-1)
res244: Int = -1
```

##### Try

For `Try` types, `getOrElse` works almost just like it does for `Option`, providing the value inside of the `Success` if it's a `Success`, and if it's a `Failure`, producing the value passed in as its argument.

```scala
scala> val successful: Try[String] = Success("hello")
successful: scala.util.Try[String] = Success(hello)

scala> val failed: Try[String] = Failure(new Exception("oops"))
failed: scala.util.Try[String] = Failure(java.lang.Exception: oops)

scala> successful.getOrElse("failure")
res245: String = hello

scala> failed.getOrElse("failure")
res246: String = failure

scala> successful.map(_ + ", ").map(_ + "world!").getOrElse("failure")
res247: String = hello, world!

scala> failed.map(_ + ", ").map(_ + "world!").getOrElse("failure")
res248: String = failure
```

##### Either

For `Either` the same applies as for the above `Try` and `Option` types, except you have to declare a projection first.  If your value is the projection you chose to operate on, you get back the value inside of that projection.  Otherwise, you get back the value you passed into `getOrElse` as an argument.

```scala
scala> val leftE: Either[Int, String] = Left(5)
leftE: scala.util.Either[Int,String] = Left(5)

scala> val rightE: Either[Int, String] = Right("hello")
rightE: scala.util.Either[Int,String] = Right(hello)

scala> leftE.left.getOrElse(-33)
res249: Int = 5

scala> leftE.right.getOrElse("It was a Left")
res251: String = It was a Left

scala> rightE.left.getOrElse(-33)
res252: Int = -33

scala> rightE.right.getOrElse("It was a Right")
res253: String = hello

scala> rightE.right.getOrElse("It was a Right") + ", world!"
res254: String = hello, world!

scala> rightE.right.map(_ + ", world!").right.getOrElse("It was a Right")
res255: String = hello, world!
```

#### Comprehensions

Now, we get to tie everything back together with Comprehensions.  You will find that Comprehensions will be one of the most powerful Scala features for helping you easily work with abstract types like `Option`, `Try`, and `Either`.

If you remember with `List`, Comprehensions allowed us to pull the value(s) from one or more `List`s and used them together in an operation via a single statement.  For types, it works just the same; Comprehensions will allow us to pull values from multiple type instances of the same type and use them together in the same operation. 

When using comprehensions on abstract types, think of each type as a `List` that may hold one value, or no values.  We use the `for` clause of the comprehension to get the values out of our abstract types, and use an `if` clause to place conditions or restrictions on our results (for instance, to create a filter on which results get returned, like the `WHERE` clause in SQL).

It's also worth noting that comprehensions will keep us within our type.  For example, if we are working with `Option`s, we will get our result back within the context of the `Option` type.

Let's start by looking at some homogenous type examples.

##### Option

First, let's look at a simple "hello world" example where we are working with all `Some` values so that you can have a refresher of the Scala syntax for comprehensions and get you back into using this Scala feature gently.

```scala
scala> val hello: Option[String] = Some("hello")
hello: Option[String] = Some(hello)

scala> val comma: Option[String] = Some(", ")
comma: Option[String] = Some(, )

scala> val world: Option[String] = Some("world")
world: Option[String] = Some(world)

scala> val exclamationPoint: Option[String] = Some("!")
exclamationPoint: Option[String] = Some(!)

scala> for (
     | helloStr <- hello;
     | commaStr <- comma;
     | worldStr <- world;
     | exclamationStr <- exclamationPoint
     | ) yield (helloStr + commaStr + worldStr + exclamationStr)
res256: Option[String] = Some(hello, world!)
```

Okay, this is pretty cool, right?  In just a few lines of code, I can extract the values out of multiple `Option` instances and write an expression at the end (in the `yield` block) that declares how to aggregate the results.

So, what happens if I have some `None` values in there?  Well, if there is a `None` value in our comprehension expression, the result will be a `None`.  This is the reason for why comprehensions operate within our type - so that there is a value for expressing when a negative/empty value is encountered.

Here is an illustration of what happens when one of the `Options` operated on is a `None`:

```scala
scala> val nothing: Option[String] = None
nothing: Option[String] = None

scala> for (
     | helloStr <- hello;
     | commaStr <- comma;
     | worldStr <- nothing;
     | exclamationStr <- exclamationPoint
     | ) yield (helloStr + commaStr + worldStr + exclamationStr)
res258: Option[String] = None
```

Now, let's look at one that's got a conditional in it.

```scala
scala> for (
     | helloStr <- hello;
     | commaStr <- comma;
     | worldStr <- world;
     | exclamationStr <- exclamationPoint;
     | triggerNum <- trigger
     | if triggerNum != 0
     | ) yield (helloStr + commaStr + worldStr + exclamationStr)
res259: Option[String] = None

scala> for (
     | helloStr <- hello;
     | commaStr <- comma;
     | worldStr <- world;
     | exclamationStr <- exclamationPoint;
     | triggerNum <- trigger
     | if triggerNum == 0
     | ) yield (helloStr + commaStr + worldStr + exclamationStr)
res260: Option[String] = Some(hello, world!)
```

The above illustrates conditions pretty well.  If the condition in the `if` statement of the comprehension evaluates to `true`, you get back the result of running the operation in the `yield` expression.  Otherwise, if the `if` expression evaluates to `false`, you get back a `None`.

As you can see, here, there's a lot of power and expression in the comprehension syntax for these abstract types.

##### Try

`Try` works much the same as `Option` does for comprehensions.  If the values are `Success` values, your comprehension expression will extract the values out of those `Success` instances and apply them t o the expression in the `yield` portion to produce an aggregated result.  Otherwise, you will get back the first `Failure` instance encountered.

Let's look at a basic example:

```scala
scala> val sideA: Try[Int] = Success(5)
sideA: scala.util.Try[Int] = Success(5)

scala> val sideB: Try[Int] = Success(12)
sideB: scala.util.Try[Int] = Success(12)

scala> val sideC: Try[Int] = Success(13)
sideC: scala.util.Try[Int] = Success(13)

scala> val failed: Try[Int] = Failure(new Exception("oops"))
failed: scala.util.Try[Int] = Failure(java.lang.Exception: oops)

scala> for (
     | a <- sideA;
     | b <- sideB;
     | c <- sideC
     | ) yield (a + b + c)
res261: scala.util.Try[Int] = Success(30)

scala> for (
     | a <- sideA;
     | x <- failed;
     | b <- sideB;
     | y <- failed0;
     | c <- sideC
     | ) yield (a + b + c)
res262: scala.util.Try[Int] = Failure(java.lang.Exception: oops)

scala> for (
     | a <- sideA;
     | x <- failed0;
     | b <- sideB;
     | y <- failed;
     | c <- sideC
     | ) yield (a + b + c + x)
res263: scala.util.Try[Int] = Failure(java.lang.Exception: whoops)
```

If you supply a predicate, you will get a `NoSuchElementException` back that is wrapped in a `Failure`.

For this next example, let's only return the perimeter of our triangle if it's a right triangle to illustrate how `if` conditionals work in `Try` comprehensions:

```scala
scala> for (
     | a <- sideA;
     | b <- sideB;
     | c <- sideC
     | if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
     | ) yield (a + b + c)
res264: scala.util.Try[Int] = Success(30)

scala> val sideC: Try[Int] = Success(14)
sideC: scala.util.Try[Int] = Success(14)

scala> for (
     | a <- sideA;
     | b <- sideB;
     | c <- sideC
     | if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
     | ) yield (a + b + c)
res265: scala.util.Try[Int] = Failure(java.util.NoSuchElementException: Predicate does not hold for 14)
```

##### Either

With `Either`, comprehensions work no differently, except to pull the value out of your type, you need to tell it which projection you want to work with each time you pull a value out of an `Either` instance in your `for` expression.

Here are some examples.  They get pretty involved, so read them closely:

```scala
scala> val leftE: Either[Int, String] = Left(-3)
leftE: scala.util.Either[Int,String] = Left(-3)

scala> val hello: Either[Int, String] = Right("hello")
hello: scala.util.Either[Int,String] = Right(hello)

scala> val world: Either[Int, String] = Right("world")
world: scala.util.Either[Int,String] = Right(world)

scala> val comma: Either[Int, String] = Right(", ")
comma: scala.util.Either[Int,String] = Right(, )

scala> val leftA: Either[Int, String] = Left(5)
leftA: scala.util.Either[Int,String] = Left(5)

scala> val leftB: Either[Int, String] = Left(12)
LeftB: scala.util.Either[Int,String] = Left(12)

scala> val leftC: Either[Int, String] = Left(14)
LeftC: scala.util.Either[Int,String] = Left(14)

scala> for (
     | helloStr <- hello.left;
     | commaStr <- comma.left;
     | worldStr <- world.left
     | ) yield (helloStr + commaStr + worldStr)
res268: scala.util.Either[Int,String] = Right(hello)

scala> for (
     | helloStr <- hello.right;
     | commaStr <- comma.right;
     | worldStr <- world.right
     | ) yield (helloStr + commaStr + worldStr)
res270: scala.util.Either[Int,String] = Right(hello, world)

scala> for (
     | helloStr <- hello.right;
     | a <- leftA.left;
     | b <- leftB.left
     | ) yield (helloStr + (a + b).toString)
<console>:25: warning: a type was inferred to be `Any`; this may indicate a programming error.
       helloStr <- hello.right;
                ^
res273: scala.util.Either[Any,String] = Left(hello17)

scala> for (
     | helloStr <- hello.right;
     | a <- leftA.right;
     | b <- leftB.right
     | ) yield (helloStr + a + b)
res277: scala.util.Either[Int,String] = Left(5)

scala> for (
     | a <- leftA.left;
     | b <- leftB.left;
     | c <- leftC.left
     | ) yield (a + b + c)
res281: scala.util.Either[Int,String] = Left(31)

scala> for (
     | a: Int <- leftA.left;
     | b: Int <- leftB.left;
     | c: Int <- leftC.left
     | if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
     | ) yield (a + b + c)
<console>:27: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       a: Int <- leftA.left;
                       ^
<console>:28: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       b: Int <- leftB.left;
                       ^
<console>:29: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       c: Int <- leftC.left
                       ^
<console>:30: error: type mismatch;
 found   : Int => Boolean
 required: scala.util.Either[Int,Nothing] => Boolean
       if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
                                          ^

```

In the above examples, if you read closely, you can see that the comprehension expression either returns the value of the computation in the `yield` expression if a value is found for each `Either` instance in the expected projection, or else the comprehension returns the value of the first `Either` instance whose value is the other projection.  That's the basic understanding of comprehensions using the `Either` type.  You DO have to remember to declare which projection you want every time you want to extract a value from an `Either` instance in your comprehensions.

It's important to pay close attention to the above examples, because the implementation of `Either` has some tricky aspects and, some even say flaws, regarding its implementation.  Perhaps the most important thing to note is that the type system cannot figure out how to handle `if` conditionals in `Either` comprehensions because it doesn't know how to figure out what the default value should be for the result should the conditional evaluate to `false`.  Even more importantly, the error in such a case is a bit convoluted and difficult to understand, especially for Scala beginners, so it's best to remember that `Either` comprehensions don't support `if` conditionals for filtering the results.

The other main thing to note is that if you try to get values out of the `left` of some `Either` instances, and the `right` on others, Scala will often get confused and issue a warning saying it couldn't resolve the types defaulting the unknown type parameters for the `Either` to `Any`.  This warning may happen even if all of the `Either` instances you extract values from are of the same type.  I'm not sure why this issue exists, but my guess is it's due to an unresolved bug in the compiler.

#### Putting It All Together

Let's take a last look at our old and overused example of the Apache HttpClient.  This is the code we left off with:

```scala
scala> import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.util.{Try, Success, Failure, Either, Left, Right}

scala> import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultHttpClient

scala> import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}

scala> import org.apache.http.message.BasicStatusLine
import org.apache.http.message.BasicStatusLine

scala> import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}
import org.apache.http.{StatusLine, ProtocolVersion, HttpResponse, HttpEntity}

scala> import com.redis._
import com.redis._

scala> val redis: Try[RedisClient] = Try(new RedisClient("localhost", 6379))
redis: scala.util.Try[com.redis.RedisClient] = Success(localhost:6379)

scala> redis.map(_.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home"))
res286: scala.util.Try[Boolean] = Success(true)

scala> val httpClient: Try[DefaultHttpClient] = { redis
     |   .map(_.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home"))
     |   .filter(_ == true)
     |   .map(_ => new DefaultHttpClient()) }
httpClient: scala.util.Try[org.apache.http.impl.client.DefaultHttpClient] = Success(org.apache.http.impl.client.DefaultHttpClient@5304d614)

scala> def handleHttpResponse(httpResponse: CloseableHttpResponse): Try[Either[StatusLine, HttpEntity]] = {
     |   val statusLine = httpResponse.getStatusLine()
     |   Try(statusLine.getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code if 300 until 500 contains code => Left(statusLine)
     |     case code => throw new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase())
     |   })
     | }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val httpResponse: Try[Option[Try[Either[StatusLine, HttpEntity]]]] = { redis.map(
     |   _.get("uri-requested").map(uri => httpClient.map(_.execute(new HttpGet(uri))).flatMap(handleHttpResponse(_)))
     | ) }
httpResponse: scala.util.Try[Option[scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]]] = Success(Some(Success(Right(org.apache.http.conn.BasicManagedEntity@1d358f17))))

scala> httpResponse.flatMap(
     |   _.fold(Failure(new Exception("No URI at Redis key")): Try[Either[StatusLine, HttpEntity]])(identity(_))
     |   .map(_.fold(statusLine => println(statusLine.getReasonPhrase()), httpEntity => println(httpEntity.getContent().read())))
     | ).fold(System.err.println(_), _ => Unit)
60

scala>
```

With our latest bag of tricks, `orElse`, `getOrElse`, and Comprehensions, the above code can be turned into the following:

```scala
scala> val redisClient: Try[RedisClient] = Try(new RedisClient("localhost", 6379))
redis: scala.util.Try[com.redis.RedisClient] = Success(localhost:6379)

scala> val httpClient: Try[DefaultHttpClient] = { redisClient
     |   .map(_.set("uri-requested", "http://dstconnect.dstcorp.net/display/dstconnect/Home"))
     |   .filter(_ == true)
     |   .map(_ => new DefaultHttpClient()) }
httpClient: scala.util.Try[org.apache.http.impl.client.DefaultHttpClient] = Success(org.apache.http.impl.client.DefaultHttpClient@3d3caa14)

scala> def handleHttpResponse(httpResponse: CloseableHttpResponse): Try[Either[StatusLine, HttpEntity]] = {
     |   val statusLine = httpResponse.getStatusLine()
     |   Try(statusLine.getStatusCode() match {
     |     case code if 100 until 227 contains code => Right(httpResponse.getEntity())
     |     case code if 300 until 500 contains code => Left(statusLine)
     |     case code => throw new Exception("Got status code: " + code.toString + " with message: " + statusLine.getReasonPhrase())
     |   })
     | }
handleHttpResponse: (httpResponse: org.apache.http.client.methods.CloseableHttpResponse)scala.util.Try[scala.util.Either[org.apache.http.StatusLine,org.apache.http.HttpEntity]]

scala> val result: Try[Unit] = for (
     |   uri <- redisClient.map(_.get("uri-requested").getOrElse(throw new Exception("No URI found at Redis key")));
     |   res <- httpClient.map(_.execute(new HttpGet(uri))).flatMap(handleHttpResponse(_))
     | ) yield (
     |   res.fold(statusLine => println(statusLine.getReasonPhrase()), httpEntity => println(httpEntity.getContent.read()))
     | )
result: scala.util.Try[Unit] =
Failure(java.lang.IllegalStateException: Invalid use of BasicClientConnManager: connection still allocated.
Make sure to release the connection before allocating another one.)

scala> result.fold(System.err.println(_), _ => Unit)
java.lang.IllegalStateException: Invalid use of BasicClientConnManager: connection still allocated.
Make sure to release the connection before allocating another one.
res7: Any = ()

scala>
```

You may argue whether this code is better than the code above, but at least you can see here that you have some flexibility and options in terms of how you want to express the behavior of your program without sacrificing the type safety of your code. I think this last example is the easiest to read of all of the others for this particular code sample.

# Conclusions

It must be admitted that this article's purpose is not only to serve as a kind of tutorial for the Scala error handling types and their capabilities, but also as an investigation into the usefulness of these types.  I will talk about my conclusions in a Pros and Cons list below.

## Pros

- Compared to traditional Java techniques such as using `try ... catch ... finally` and returning `null`, the Scala error handling types enable the programmer to handle most errors in a type-safe way that reduces the kinds of possible runtime errors that are possible.  Examples of this are the use of `Option` in place of `null` enables the compiler to ensure that the `null` case is handled in most circumstances (which, in-turn, all but eliminates the possibility of a runtime `NullPointerException`).  Similarly, using `Try` instead of `try ... catch ... finally` ensures that the programmer is aware of the possibility of `Exceptions` occurring, and the compiler will not allow code to be written that overlooks this possibility (mitigating the problem of uncaught exceptions remaining unhandled and bubbling up to the more abstract parent functions, where they are more difficult to troubleshoot).
- There is more power and expressivity in using the error handling types.  In using the Collections functions such as `map`, `flatMap`, `fold`, `orElse`, `getOrElse`, and comprehensions, error reporting and side-effecting code can be deferred until later, pushing side effects out to the edges of the codebase and encouraging a programming style that prefers to keep the core of the program immutable, comprised of small functions, and easy to test and reason about.  It is also easier, with these high-level functions and language features, to reduce the level of nesting in the code, which typically makes it easier to read and reason about.  In addition to this, the `Either` type provides a powerful data structure for handling non-critical errors in a way that does not propogate run-time throwables, as well as a way to express functions whose return value is best utilized as a disjoint union.
- It's not mentioned in the above article, but the high-level collections functions on the types are easily parallelizable, having `par` implementations on the Scala Parallel Collections libraries.  This may be a topic for future research to investigate the true power and capabilities of Scala's parallel collections libraries.

## Cons and Shortcomings

- Although the types do provide a lot of additional type safety without sacrificing much flexibility (and especially gaining flexibility over Java), functions like `foreach`, `get`, `rescue`, etc. can still leave the door open for errors such as `Exceptions`  getting "swallowed", or, in the case of `get`, allowing exceptions to be rethrown outside of the type, which enables them to propogate unhandled up through the code base to the highest level functions (the main problem the `Try` type is there to solve).  Despite this shortcoming, the API still makes it pretty obvious to the programmer when he is doing something "dangerous", so this doesn't completely negate the advantages of the types, here, over the traditional Java techniques.
- The APIs for some of the types seem incomplete.  For example, `Try` did not have a `fold` implementation until the most recent version of Scala (2.12.0), and `Either` comprehensions can throw error messages warning that `withFilter` isn't implemented by the `LeftProjection` or `RightProjection` yet (but the messages indicate that they might have plans to implement them in the future?).  This makes the APIs for each of the types harder to reason about because it's not obviously apparent which functions are supported by which types without closely studying the documentation or experimenting a lot with the types in the REPL.
- Instead of implemented many of the Collections API functions directly on each type, and using `trait`s or super types to mixin the functions required for each abstract type, many of the functions were implemented on `Option`, and then a `toOption` function was provided on each type (or, in the case of `Either`, on each projection of the type).  This makes it somewhat unclear, like in the above, which functions to expect on each type, and adds the problem that to work with certain functions, I can't remain in my current type, but must move my data into the `Option` context.  This also makes it unclear regarding what exactly these types have in common that allows them all to have implementations of the same functions (is it because the type is a special case of `Functor`, or `Monoid`, or `Applicative`, ... ? Or is something not implemented on `Either` because it cannot be resolved to an instance of `Alternative`? ).  This often seems to be the culprit of covoluted error messages like the following:
```scala
scala> for (
     | a: Int <- leftA.left;
     | b: Int <- leftB.left;
     | c: Int <- leftC.left
     | if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
     | ) yield (a + b + c)
<console>:27: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       a: Int <- leftA.left;
                       ^
<console>:28: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       b: Int <- leftB.left;
                       ^
<console>:29: warning: `withFilter' method does not yet exist on scala.util.Either.LeftProjection[Int,String], using `filter' method instead
       c: Int <- leftC.left
                       ^
<console>:30: error: type mismatch;
 found   : Int => Boolean
 required: scala.util.Either[Int,Nothing] => Boolean
       if Math.pow(a, 2) + Math.pow(b, 2) == Math.pow(c, 2)
       ^
```
- It takes some effort and study to grok these more abstract types for people coming from languages like Java, C++, and especially Python and Ruby (being dynamically typed languages).  Abstract types that build on the concepts of functors, monoids, monads, arrows, and the like challenge traditional programmers to step-up their comprehension of the patterns that occur in programming to a much more abstract level.  I set out to write a short article on this topic, but after some discussions with people on the team and other programmers in my circle of friends and acquaintences, I decided to take a more lengthy and detailed approach, recognizing that people seeing abstract datatypes for the first time were having a more difficulty than I had anticipated in wrapping their heads around how these types work.

## References and Further Reading

1. http://danielwestheide.com/scala/neophytes.html
2. http://www.scala-lang.org/api/2.11.7/index.html#package
3. http://www.scala-lang.org/files/archive/api/2.12.0-M1/#package
4. https://wiki.haskell.org/Typeclassopedia
5. http://alvinalexander.com/scala/using-scala-option-some-none-idiom-function-java-null
6. https://hc.apache.org/httpcomponents-client-ga/
7. http://www.mkyong.com/java/apache-httpclient-examples/
8. https://github.com/debasishg/scala-redis
9. http://mauricio.github.io/2014/02/17/scala-either-try-and-the-m-word.html

