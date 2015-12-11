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

#### `flatMap`
`flatMap` is like `map`, except it `flatten`s out the result `List` in the event that this list is multidimensional (a.k.a the List contains other Lists).

E.g. *(in the Scala REPL)*
```scala
scala> val xs: List[Int] = List(1, 2, 4)
scala> xs.flatMap(n => List(n + 1))
res0: List[Int] = List(2, 3, 5)

scala> xs
res1: List[Int] = List(1, 2, 4)
```

As with `map`, `flatMap` should not be used for operations which may cause side effects.

#### `flatten`
`flatten` is pretty simple once you understand `flatMap`.  As you might guess, all it does is take a multidimensional list and make it one dimensional.

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

#### `contains`
`contains` is just a simple function for checking whether a `Collection` such as a `List` `contains` a particular value.  If the `Collection` `contains` the value passed in as it's argument, `contains` returns `true`, otherwise, it returns `false`.

E.g.
```scala
scala> val xs: List[Int] = List(1, 2, 3)
scala> xs.contains(1)
res0: Boolean = true

scala> xs.contains(5)
res1: Boolean = false
```

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

#### `head`
`head` returns the first element of the `List` or `Collection`, or throw a `NoSuchElementException` if you happen to call `head` on an empty `Collection` (such as a `List`).

```scala
scala> val x: List[Int] = List(1, 2)
scala> x.head
res0: Int = 1

scala> val y: List[Int] = List()
scala> y.head
java.util.NoSuchElementException: head of empty list
  at scala.collection.immutable.Nil$.head(List.scala:420)
  at scala.collection.immutable.Nil$.head(List.scala:417)
  ... 43 elided
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
```

As you can see from the above, `forall` (and also `filter` and other related functions that take a lambda) can be quite handy, but you have to be careful to satisfy the type system when writing in your logic.  Most type errors should be caught by the Scala compiler, so if you have a good IDE, it should tell you when you're doing something that the Scala compiler doesn't like, but it should also be apparent from the error message above that you might not always get the most helpful error responses (take a look at the `scala.MatchError` when calling `forall` on `y` for the first time).  The best way to guard against running into these sorts of issues is to make sure you **1.** always provide a default case at the end to ensure all of your possible edge cases have a match, and **2.** make sure you don't test for cases that could never occur, such as in the last call of `forall` on `x` in the above example, where we try to match the contents of `x` on the `String` type when we already know via the type declaration on `x` that all of the items are `Int`s.  Scala's type system will catch such senseless attempts and actually prevent your code from compiling in these cases.

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
You might notice from the above REPL printout that the creation of the RedisClient object doesn't actually check that it can establish a connection to Redis, but let's assume that it could, and would through an exception if it couldn't connect.  You can see that we have to violate one of the principals of Scala, immutability, in order to create our Redis client.  This situation will occur most often when using Java libraries from Scala that could throw exceptions when instantiating the class (such as when creating an instance of a class that serves as a server client - if it cannot connect to the server, it will likely throw an exception).

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

scala> val valueAtKey: Try[Option[String]] = redisClient match {
     |   case Success(client) => Try(client.get("test-key"))
     |   case Failure(e) => Failure(e)
     | }
valueAtKey: scala.util.Try[Option[String]] = Failure(java.lang.RuntimeException: java.lang.StackOverflowError)

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
The above code will behave exactly the same as the previous code block.  So, even at this point, without understanding, yet, how to use comprehensions and `Collection`s functions on these datatypes, we can see that we have the power to drastically simplify code which needs to handle the possibility of `Throwable`s.

#### Either
`Either` is a lot like `Try`, in that it can be one of two values.  However, while `Try` is either a `Success` that holds the value returned from a function or operation when it successfully completes or a `Failure` that holds the `Throwable` which contains information about the error that occurred while trying to perform the operation, `Either` can be 'either' a `Right`, which, by convention, normally contains the value an operation returns for the expected, or successful, value (think, if you get a `Right`, things went 'right'), or else `Either` could be a `Left`, which normally holds some less-happy value such as an error (but it doesn't have to indicate an error).

##### When to Use Either Over Try
Some people out there in Google-land will tell you that `Either` is less preferred or inferior to the `Try` type and normally indicates a code-smell.  I'm not ready, yet, to draw that particular conclusion, but there is a distinction that needs to be made in purpose between `Try` and `Either` so that you know when to use each and how to design your APIs.

The main thing to realize is that `Try` is meant specifically for the purpose of handling `Exception`s.  Intelligently using `Either` vs. `Try`, therefore, requires some discipline around when you choose to throw `Exceptions` vs. just notify the application that some sort of error or issue that is expected to be common and non-critical arises.  For the latter case, `Either` is a better choice than `Try`.  The other main purpose for `Either` is when you have a function which may return one of two types, but neither type returned necessarily indicates an error.  Such cases as these are fairly uncommon, but they do arise from time to time, so it's good to know that the `Either` type is there if you ever run into such a scenario where you DO need a disjoint union as your return type.  Most of the time, though, it helps to think of `Either` as an alternative to `Option` that returns some information about why you have a `None` when a `None` occurs, rather than just being given back `None` with no explanation.

In the case of the scala-redis client's `get` method that we looked at previously, `Option` was fine because there's really only a few reasons for why you wouldn't get a value back from a key - either the key doesn't exist, or it does exist but it's value is `null`.  Either way, from a programming standpoint, it doesn't really matter most times what caused `get` to return `None`, what matters is that we know there is no value at that key.

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

Now, as you read this, you might think that this is all very interesting, but what happens when I want to get the value out of my `Either`?  I can't just `get` it like I can for `Option` and `Try` because it's not obvious that `Right` is necessarily the only truly successful value.  `Either` could be used to return a disjoint union where both possible response types are equally successful value. So, let's look at some examples for how to get the value out of our `Either`.

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


However, what if we already knew it was a `Right`, or a `Left`, or were expecting it to be one or other do the degree that we would want an `Exception` to be thrown if it wasn't the `Either` value we were expecting?  Well, we can 'project' any `Either` instance as a `left` projection or a `right` projection.  They both work the same.  If I project my `Either` as a `Right` and it is a `Right`, then if I call `get` on that projection, I get back the value inside of the `Right`.  However, if it's a `Left`, it will throw an `Exception`.  The same goes for the `Left` projection, except `get` throws an `Exception` if it's a `Right`, and the value inside of the `Left` otherwise.

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
