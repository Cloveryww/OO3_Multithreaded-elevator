package oo3;

class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
    public void uncaughtException(Thread t,Throwable e) {
        System.out.println("nothing to do!");
    }
}
