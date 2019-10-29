package com.gp.rainy;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxJava1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //它决定事件触发的时候将有怎样的行为
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        };

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        };

        //决定什么时候触发事件以及触发怎样的事件
        //OnSubscribe 会被存储在返回的 Observable 对象中，它的作用相当于一个计划表，当 Observable 被订阅的时候，
        // OnSubscribe 的 call() 方法会自动被调用
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>(){

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }
        });

        Observable<String> observable1 = Observable.just("hello", "hi", "hehe");


        String[] words = {"Hello", "Hi", "Aloha"};
        Observable<String> observable2 = Observable.from(words);

        observable.subscribe(observer);
        observable.subscribe(subscriber);
        //observable1
        //observable2

//        public Subscription subscribe(Subscriber subscriber) {
//            subscriber.onStart();
//            onSubscribe.call(subscriber);
//            return subscriber;
//        }

        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {

            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        };

        Action0 onCompletedAction = new Action0() {
            @Override
            public void call() {

            }
        };

        // 自动创建 Subscriber ，并使用 onNextAction 来定义 onNext()
        observable.subscribe(onNextAction);
        observable.subscribe(onNextAction, onErrorAction);
        observable.subscribe(onNextAction, onErrorAction, onCompletedAction);
        //observable1
        //observable2


        String[] names = {"2", "2"};

        Observable.from(names).
                subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                    }
                });
        
        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                int drawableRes = 3;
                Drawable drawable = getDrawable(drawableRes);
                subscriber.onNext(drawable);
                subscriber.onCompleted();;
            }
        }).subscribe(new Observer<Drawable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Drawable drawable) {
                ImageView imageView = null;
                imageView.setImageDrawable(drawable);
            }
        });

        //在不指定线程的情况下， RxJava 遵循的是线程不变的原则，
        // 即：在哪个线程调用 subscribe()，就在哪个线程生产事件；在哪个线程生产事件，就在哪个线程消费事件


//        Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
//        Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
//        Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。行为模式和 newThread() 差不多，
// 区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率。
// 不要把计算工作放在 io() 中，可以避免创建不必要的线程。
//        Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
//        另外， Android 还有一个专用的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。





//        subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
//        observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。


        Observable.just(1,2,3,4)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                    }
                });

        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                int drawableRes = 3;
                Drawable drawable = getDrawable(drawableRes);
                subscriber.onNext(drawable);
                subscriber.onCompleted();;
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Drawable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Drawable drawable) {
                ImageView imageView = null;
                imageView.setImageDrawable(drawable);
            }
        });


        Observable.just("images/logo.png")
                .map(new Func1<String, Bitmap>() {

                    @Override
                    public Bitmap call(String s) {
                        String ss = s;
//                        return getBitmapFromPath(s);
                        return null;
                    }
                }).subscribe(new Action1<Bitmap>() {

            @Override
            public void call(Bitmap bitmap) {

            }
        });

        class Student{

        }
        class Course{

        }

        Student[] students = null;

        Subscriber<String> subscriber1 = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        };
        Observable.from(students)
                .map(new Func1<Student, String>() {
                    @Override
                    public String call(Student student) {
                        //return student.getname;
                        return null;
                    }
                })
                .subscribe(subscriber1);

        Subscriber<Course> subscriber2 = new Subscriber<Course>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Course course) {

            }
        };

        Observable.from(students)
                .flatMap(new Func1<Student, Observable<Course>>() {
                    @Override
                    public Observable<Course> call(Student student) {

//                        return Observable.from(student.getCourses());
                        return null;
                    }
                })
                .subscribe(subscriber2);

//        flatMap() 的原理是这样的：
//          1. 使用传入的事件对象创建一个 Observable 对象；
//          2. 并不发送这个 Observable, 而是将它激活，于是它开始发送事件；
//          3. 每一个创建出来的 Observable 发送的事件，都被汇入同一个 Observable ，
//          而这个 Observable 负责将这些事件统一交给 Subscriber 的回调方法。



//        networkClient.token() // 返回 Observable<String>，在订阅时请求 token，并在响应后发送 token
//                .flatMap(new Func1<String, Observable<Messages>>() {
//                    @Override
//                    public Observable<Messages> call(String token) {
//                        // 返回 Observable<Messages>，在订阅时请求消息列表，并在响应后发送请求到的消息列表
//                        return networkClient.messages();
//                    }
//                })
//                .subscribe(new Action1<Messages>() {
//                    @Override
//                    public void call(Messages messages) {
//                        // 处理显示消息列表
//                        showMessages(messages);
//                    }
//                });
        
        
        
        //    throttleFirst(): 在每次事件触发后的一定时间间隔内丢弃新的事件。常用作去抖动过滤


//        变换的原理：lift()
//    换虽然功能各有不同，但实质上都是针对事件序列的处理和再发送
//        而在 RxJava 的内部，它们是基于同一个基础的变换方法： lift(Operator)

//        public <R> Observable<R> lift(Operator<? extends R, ? super T> operator) {
//            return Observable.create(new OnSubscribe<R>() {
//                @Override
//                public void call(Subscriber subscriber) {
//                    Subscriber newSubscriber = operator.call(subscriber);
//                    newSubscriber.onStart();
//                    onSubscribe.call(newSubscriber);//利用这个新 Subscriber 向原始 Observable 进行订阅
//                }
//            });
//        }



//        这个新 OnSubscribe 的 call() 方法中的 onSubscribe ，就是指的原始 Observable 中的原始 OnSubscribe ，
//        在这个 call() 方法里，新 OnSubscribe 利用 operator.call(subscriber) 生成了一个新的
//        Subscriber（Operator 就是在这里，通过自己的 call() 方法
//        将新 Subscriber 和原始 Subscriber 进行关联，并插入自己的『变换』代码以实现变换），
//        然后利用这个新 Subscriber 向原始 Observable 进行订阅



//        observable.lift(new Observable.Operator<String, Integer>() {
//            @Override
//            public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
//                // 将事件序列中的 Integer 对象转换为 String 对象
//                return new Subscriber<Integer>() {
//                    @Override
//                    public void onNext(Integer integer) {
//                        subscriber.onNext("" + integer);
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        subscriber.onCompleted();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        subscriber.onError(e);
//                    }
//                };
//            }
//        });


//        Observable 还有一个变换方法叫做 compose(Transformer)。
//        它和 lift() 的区别在于， lift() 是针对事件项和事件序列的，
//        而 compose() 是针对 Observable 自身进行变换。



        //doOnNext

    }

    


    @Override
    protected void onStart() {
        super.onStart();
    }
}
