* What Is SimSQL?

SimSQL is system for _stochastic analytics_ implemented at [Rice University](http://www.rice.edu/).

At its most basic, SimSQL is a scalable, parallel, analytic relational database system compiles SQL queries into Java codes that run on top of [Hadoop](http://hadoop.apache.org/). We have regularly used SimSQL to run SQL queries on 100-node Hadoop clusters with 1,000+ compute cores and terabytes of data.

In the sense that SimSQL is an SQL-based platform that runs on top of Hadoop, it is similar to platforms such as [Hive](http://hive.apache.org/). However, compared to systems such as Hive, SimSQL is much more of a "classical" relational database system. It has a fully functional query optimizer, and it doesn't just use an "SQL-like" scripting language: the language supported by SimSQL is very close to classical SQL, with full support for important SQL language constructs such as nested subqueries.

* Stochastic Analytics Using SimSQL.

If all you do is use SimSQL as a parallel database system for running analytic queries, you will find it to be very useful. It has many of the performance optimizations found on commercial database systems, such as the ability to choose from a number of different join algorithms, and the ability to automatically pipeline operations.

But what makes SimSQL truly unique is its support for stochastic analytics. SimSQL has special facilities that allow a user to define special database tables that have _simulated_ data---these are data that are not actually stored in the database, but are produced by calls to statistical distributions. Such simulated data can be queried just like any other database data. This is very useful because it allows one to use statistical distributions in place of data that are uncertain. In real life, uncertain data are commonplace due to measurement errors, because they have not yet been observed and must be forecast (think of sales figures for the upcoming quarter), or because they were never recorded and must be imputed. As long as you can come up with an appropriate statistical model, it is possible to use SimSQL to ask (and answer) sophisticated questions such as "What would my profits have been last year had I raised my profits by 10%?"

Crucially, when a query is issued over a table containing simulated data, an entire distribution of query results are returned. This distribution gives a user an indication of the uncertainty in the query result due to uncertainty in the data.

* Bayesian Machine Learning Using SimSQL.

SimSQL can also be used for large-scale [Bayesian machine learning](http://mlg.eng.cam.ac.uk/zoubin/bayesian.html), which can be viewed as a special case of stochastic analytics.

The most common way in which Bayesian machine learning models are learned is via [_Markov Chain Monte Carlo_](http://en.wikipedia.org/wiki/Markov_chain_Monte_Carlo), or MCMC. To perform MCMC on "Big Data", a platform needs to be able to run Markov chain simulations on large data sets, in which new data are simulated via a set of recursive dependencies. Since tables of simulated data in SimSQL can have such recursive dependencies, it is easy to use SimSQL to run Markov Chain simulations over "Big Data". This means that SimSQL is a great platform very large-scale Bayesian machine learning. In fact, we have amassed a great deal of experimental evidence showing that SimSQL scales as well as (or better than) many other platforms for large-scale machine learning.

* How Do I Get Started With SimSQL?

SimSQL is entirely open-source. We do most of our development on top of the [Amazon EC2](http://aws.amazon.com/ec2/) platform. In the **docs** folder of this repository, you'll find enough information to have SimSQL up and running on Amazon EC2 within about 30 minutes, even if you have somewhat limited computer systems skills. All you have to do is rent an Amazon EC2 machine and have it run the SimSQL machine image that we've created---if you can do that, it won't take you long to have a SimSQL Hadoop cluster up and running.

If you are interested in developing SimSQL we have also created an image that runs Hadoop in pseudo distributed mode within a docker image and enables you to do line by line debugging. The instructions on how to setup a development environment can be found in the **docs** folder of this repository.

* Additional Resources.

A presentation describing SimSQL can be found [here](http://cmj4.web.rice.edu/SimSQLNew.pdf). A technical paper describing how SimSQL can be used for Bayesian machine learning is [here](https://developer.logicblox.com/wp-content/uploads/2013/10/sigmod13-foula.pdf). An experimental comparison of SimSQL with a number of other platforms for large-scale machine learning is [here](http://cmj4.web.rice.edu/performance.pdf). SimSQL is an extension of the earlier MCDB system for stochastical analytics; the definitive technical paper describing MCDB can be found [here](http://www.cs.rice.edu/~lp6/mcdb-tods.pdf).
