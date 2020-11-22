
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.awt.Composite

import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.management.RuntimeErrorException

import kotlin.math.pow
import kotlin.math.roundToInt



fun main() {
    exampleOf("fromIterable") {
        val observable: Observable<Int> = Observable.fromIterable(listOf(1))

    }

    val sequence =  0 until 3
    val iterator = sequence.iterator()
    while(iterator.hasNext()) {
        println(iterator.next())
    }

    exampleOf("subscribe") {
        val observable = Observable.just(1,2,3)
        observable.subscribe { println(it)}
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()

        observable.subscribeBy(
            onNext = {println(it)},
            onComplete = {println("Completed")}
        )
    }

    exampleOf("never") {
        val observable = Observable.never<Any>()

        observable.subscribeBy(
            onNext = { println(it)},
            onComplete = { println("Completed")}
        )
    }

    exampleOf("range") {
        val observable: Observable<Int> = Observable.range(1,10)
        observable.subscribe {

            val n = it.toDouble()
            val fibonacci = ((1.61803.pow(n) - 0.61803.pow(n)) /
                    2.23606).roundToInt()
            println(fibonacci)
        }

    }

    exampleOf("dispose") {
        val mostPopular: Observable<String> = Observable.just("A","B", "C")
        val subscription = mostPopular.subscribe {
            println(it)
        }

        subscription.dispose()
    }

    exampleOf("CompositeDisposable") {
        val subscriptions = CompositeDisposable()
        val disposable = Observable.just("A", "B", "C")
                .subscribe() {
                    print(it)
                }

        subscriptions.add(disposable)
    }


    exampleOf("create") {
        val disposable = CompositeDisposable()
        Observable.create<String>{emitter ->
            emitter.onNext("1")
            emitter.onNext("2")
            emitter.onNext("?")
        }.subscribeBy(
                onNext = { println(it)},
                onComplete = { println("Completed")},
                onError = { println(it)}
        )
    }

    exampleOf("defer") {
        val disposables = CompositeDisposable()
        var flip = false
        val factory: Observable<Int> = Observable.defer{
            flip = !flip
            if(flip) {
                Observable.just(1,2,3)
            } else {
                Observable.just(4,5,6)
            }
        }

        for(i in 0..3) {
            disposables.add(
                    factory.subscribe() {
                        println(it)
                    }
            )
        }
            disposables.dispose()
        }

        exampleOf("Single") {
            val subscriptions = CompositeDisposable()
            fun loadText(filename: String): Single<String> {
                return Single.create create@{ emitter->
                    val file = File(filename)
                    if(!file.exists()) {
                        emitter.onError(FileNotFoundException("Can't find $filename"))
                        return@create
                    }
                    val contents = file.readText(Charset.defaultCharset())
                    emitter.onSuccess(contents)
                }
            }

            val observer = loadText("Copyright.txt")
                    .subscribeBy(
                            onSuccess = {println(it)},
                            onError = { println("Error, $it")}
                    )
            subscriptions.add(observer)
        }

    exampleOf("PublishSubject") {
        val publishSubject = PublishSubject.create<Int>()
        publishSubject.onNext(0)
        val subscriptionOne = publishSubject.subscribe {
            int-> println(int)
        }
        publishSubject.onNext(1)
        publishSubject.onNext(2)
        val subscriptionThree = publishSubject.subscribeBy(
                onNext = {printWithLabel("3)", it)}

        )
        publishSubject.onNext(6)


    }

    exampleOf("BehaviorSubject") {
        val subscriptions = CompositeDisposable()
        val behaviorSubject = BehaviorSubject.createDefault("Initial value")
        behaviorSubject.onNext("X")
        val subscriptionOne = behaviorSubject.subscribeBy(
                onNext = {printWithLabel1("1)", it)},
                onError = {printWithLabel2("1)", it)}
        )
    }

    exampleOf("BehaviorSubject State") {
        val subscriptions = CompositeDisposable()
        val behaviorSubject = BehaviorSubject.createDefault(0)
        println(behaviorSubject.value)

        subscriptions.add(behaviorSubject.subscribeBy {
            printWithLabel("1)", it)
            behaviorSubject.onNext(1)
            println(behaviorSubject.value)
            subscriptions.dispose()
        })
    }

    exampleOf("ReplaySubject") {
        val subscriptions = CompositeDisposable()
        val replaySubject = ReplaySubject.createWithSize<String>(2)

        replaySubject.onNext("1")
        replaySubject.onNext("2")
        replaySubject.onNext("3")

        subscriptions.add(replaySubject.subscribeBy (
                onNext = {printWithLabel1("1)", it)},
                onError = { printWithLabel2("1)", it)}
        ))

        subscriptions.add(replaySubject.subscribeBy (
                onNext = {printWithLabel1("2)", it)},
                onError = {printWithLabel2("2", it)}
        ))

        replaySubject.onNext("4")
        subscriptions.add(replaySubject.subscribeBy (
                onNext = {printWithLabel1("3)", it)},
                onError = {printWithLabel2("3)", it)}

        ))

        exampleOf("AsyncSubject") {
            val subscriptions = CompositeDisposable()
            val asyncSubject = AsyncSubject.create<Int>()

            subscriptions.add(asyncSubject.subscribeBy (
                    onNext = {printWithLabel("1)", it)},
                    onError = {printWithLabel2("1)", it)}

            ))

            asyncSubject.onNext(0)
            asyncSubject.onNext(1)
            asyncSubject.onNext(2)

            asyncSubject.onComplete()
            subscriptions.dispose()
        }
    }

    exampleOf("ignoreElements") {
        val subscriptions = CompositeDisposable()
        val strikes = PublishSubject.create<String>()

        subscriptions.add(
                        strikes.ignoreElements()
                                .subscribeBy {
                                    println("You're out!")
                                })

        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onComplete()
    }

    exampleOf("filter") {
        val subscriptions = CompositeDisposable()
        subscriptions.add(
                Observable.fromIterable(listOf(1,2,3,4,5,6,7,8,9,10))
                        .filter {number ->
                            number > 5
                        }.subscribe {
                            println(it)
                        })


    }

    exampleOf("skip") {
        val subscriptions = CompositeDisposable()
        subscriptions.add(
                Observable.just("A", "B", "C", "D", "E", "F")
                        .skip(3)
                        .subscribe {
                            println(it)
                        }

        )

    }

    exampleOf("skipWhile") {
        val subscriptions = CompositeDisposable()
        subscriptions.add(
                Observable.just(2,2,3,4)
                        .skipWhile {
                            number -> number % 2 == 0
                        }.subscribe {
                            println(it)
                        })

    }

    exampleOf("take") {
        val subscriptions = CompositeDisposable()
        subscriptions.add(
                Observable.just(1,2,3,4,5,6)
                        .take(3)
                        .subscribe {
                            println(it)
                        })
    }

    exampleOf("takeWhile") {
        val subscriptions = CompositeDisposable()
        subscriptions.add(
                Observable.fromIterable(listOf(1,2,3,4,5,6,7,8,9,10,1))
                        .takeWhile {
                            number-> number < 5
                        }.subscribe {
                            println(it)
                        })
    }

    exampleOf("takeUntil") {
        val subscriptions = CompositeDisposable()
        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subscriptions.add(
                subject.takeUntil(trigger)
                        .subscribe {
                            println(it)
                        })

        subject.onNext("1")
        subject.onNext("2")

        trigger.onNext("X")
        subject.onNext("3") // не напечается
    }

    exampleOf("distinctUntilChanged") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(
                Observable.just("Dog", "Cat", "Cat", "Dog")
                        .distinctUntilChanged() // исключает повторы
                        .subscribe {
                            println(it)
                        })


    }

    exampleOf("toList") {
        val subscriptions = CompositeDisposable()
        val items = Observable.just("A", "B", "C")

        subscriptions.add(
                items
                        .toList() // создают массив
                        .subscribeBy {
                            println(it)
                        })


    }

    exampleOf("flatMap") {
        val subscriptions = CompositeDisposable()
        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student.flatMap { it.score }.subscribe { println(it) }.addTo(subscriptions)

        student.onNext(ryan)
        ryan.score.onNext(85) // изменяемый массив будет 80 и 85
        student.onNext(charlotte) // 80,85,90
        ryan.score.onNext(95) // 80,85,90,95
        charlotte.score.onNext(100) // 80,85,90,95, 100


    }

    exampleOf("switchMap") {
        val subscriptions = CompositeDisposable()
        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student.switchMap { it.score }.subscribe { println(it) }.addTo(subscriptions)

        student.onNext(ryan)
        ryan.score.onNext(85) // изменяемый массив будет 80 и 85
        student.onNext(charlotte) // 80,85,90
        ryan.score.onNext(95) // 80,85,90
        charlotte.score.onNext(100) // 80,85,90 100 | 95 будет отброшено



    }

    exampleOf("materialize/dematerialize") {
        val subscriptions = CompositeDisposable()
        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = BehaviorSubject.create<Student>()

        val studentScore = student.switchMap { it.score.materialize() }

        subscriptions.add(studentScore.subscribe {println(it)})
        ryan.score.onNext(85)
        ryan.score.onError(RuntimeException("Error"))
        ryan.score.onNext(90) // не напечатается после ошибки будет переключение
        student.onNext(charlotte)

        studentScore.filter {
            if(it.error != null) {
                println(it.error)  // трансформация обратно в числа
                false
            } else {
                true
            }
        }
                .dematerialize<Int>()
                .subscribe {
                    println(it)
                }.addTo(subscriptions)
    }


    exampleOf("startWith") {
        val subscriptions = CompositeDisposable()
        val missingNumbers = Observable.just(3,4,5)
        val completeSet = missingNumbers.startWith(listOf(1,2)) // добавляет в начале 1 и 2 к 3 4
        // 5

        completeSet.subscribe {
            number -> println(number)
        }.addTo(subscriptions) // начинать с
    }
    // p 166

    exampleOf("concat") {
        val subscriptions = CompositeDisposable()
        val first = Observable.just(1,2,3)
        val second = Observable.just(4,5,6)

        Observable.concat(first, second).subscribe{
            number-> println(number)
        }.addTo(subscriptions)

    }

    exampleOf("concatWith") {
        val subscriptions = CompositeDisposable()

        val countries = Observable.just("Germany", "Spain")
        val observable = countries.concatMap {
            when(it) {
                "Germany" -> Observable.just("Berlin", "Munich", "Frankfurt")
                "Spain" ->  Observable.just("Madrid", "Barcelona", "Valencia")
                else -> Observable.empty<String>() //соединяет значения одного к другому
            }
        }

        observable.subscribe{
            city-> println(city)
        }.addTo(subscriptions)

    }

    exampleOf("merge") { // слияние одного с другим
        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<Int>()
        val right = PublishSubject.create<Int>()

        Observable.merge(left, right)
                .subscribe {
                    println(it)
                }.addTo(subscriptions)

        left.onNext(0)
        left.onNext(1)
        right.onNext(3)
        left.onNext(4)
        right.onNext(5)
        right.onNext(6)
    }


    exampleOf("withLatestFrom") { // последнее активизируется так как правильно
        val subscriptions = CompositeDisposable()

        val button = PublishSubject.create<Unit>()
        val editText = PublishSubject.create<String>()

        button.withLatestFrom(editText) { _: Unit, value: String ->
            value
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        editText.onNext("Par")
        editText.onNext("Pari")
        editText.onNext("Paris")
        button.onNext(Unit)
        button.onNext(Unit)
    }

    exampleOf("reduce") { // складываем потоки вместе
        val subscriptions = CompositeDisposable()

        val source = Observable.just(1,3,5,7,9)
        source
                .reduce(0) {a,b -> a+b}
                .subscribeBy(onSuccess = {
                    println(it)
                }).addTo(subscriptions)
    }
// 25

    exampleOf("scan") { // склыдывается каждый последующий элемент
        val subscriptions = CompositeDisposable()

        val source = Observable.just(1,3,5,7,9)
        source
                .scan(0) {a,b -> a+b}
                .subscribe {
                    println(it)
                }.addTo(subscriptions)

    }

    


























    }

fun printWithLabel(s: String, it: Int) {

}
fun printWithLabel1(s: String, it: String) {

}

fun printWithLabel2(s: String, it: Throwable) {

}



fun exampleOf(s: String, function: () -> Unit) {

}
