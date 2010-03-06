package grails.plugin.jms.test

class Person {

    static hasMany = [things: Thing]
    String name

}