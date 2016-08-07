#pragma once

struct ChunkBase {
    ChunkBase *next;

    virtual ~ChunkBase() { }
};

template<typename T>
struct Chunk
    :public ChunkBase
{
    T *val;

    Chunk() {
        val = new T();
    }

    ~Chunk() {
        delete val;
    }
};

struct MemPool {
    ChunkBase *list;

    MemPool()
        :list(NULL)
    {}

    ~MemPool() {
        ChunkBase *c = list;
        while (c) {
            ChunkBase *n = c->next;
            delete c;
            c = n;
        }
    }

    template <typename T>
    T *alloc() {
        Chunk<T> *c = new Chunk<T>();

        c->next = list;
        list = c;

        return c->val;
    }
};
