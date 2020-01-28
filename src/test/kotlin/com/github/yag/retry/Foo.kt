package com.github.yag.retry

class Foo(private val errorCount: Int) : IFoo {

    var counter = 0

    override fun bar() {
        check(++counter > errorCount)
    }
}

interface IFoo {

    fun bar()

}