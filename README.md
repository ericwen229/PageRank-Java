# PageRank-Java

![license](https://img.shields.io/badge/license-MIT-brightgreen.svg)

An implemenation of the PageRank algorithm in Java.

## Usage


    PageRank rank = new PageRank();
    int a = rank.createEntity();
    int b = rank.createEntity();
    int c = rank.createEntity();
    rank.putLink(c, b);
    rank.putLink(c, a);
    rank.putLink(b, a);
    rank.runPageRank();
    double aRankValue = rank.getRankValue(a);

## TODO

* add thread safe support

# IDPool

An implementation of ID pool used to manage IDs.

## Usage

    IDPool pool = new IDPool();
    int a = pool.borrowID();
    int b = pool.borrowID();
    int c = pool.borrowID();
    // use them
    pool.returnID(a);
    pool.returnID(b);
    pool.returnID(c);
