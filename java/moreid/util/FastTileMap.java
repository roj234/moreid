/*
 * This file is a part of MI
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Roj234
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package moreid.util;

import roj.collect.AbstractIterator;
import roj.collect.CharMap;
import roj.collect.MyHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Roj234
 * @since  2020/9/27 0:41
 */
public class FastTileMap implements Map<BlockPos, TileEntity> {
    private final CharMap<TileEntity> charMap = new CharMap<>();

    private final int x, z;

    public FastTileMap(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int size() {
        return charMap.size();
    }

    @Override
    public boolean isEmpty() {
        return charMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return charMap.containsKey(posToChar((BlockPos) key));
    }

    private char posToChar(BlockPos key) {
        return (char) (((key.getX() & 15) << 12) | ((key.getZ() & 15) << 8) | key.getY());
    }

    @Override
    public boolean containsValue(Object value) {
        return charMap.containsValue(value);
    }

    @Override
    public TileEntity get(Object key) {
        return charMap.get(posToChar((BlockPos) key));
    }

    @Override
    public TileEntity put(BlockPos key, TileEntity value) {
        return charMap.put(posToChar(key), value);
    }

    @Override
    public TileEntity remove(Object key) {
        return charMap.remove(posToChar((BlockPos) key));
    }

    public CharMap<TileEntity> getInternal() {
        return charMap;
    }

    @Override
    public void putAll(Map<? extends BlockPos, ? extends TileEntity> m) {
        for (Map.Entry<? extends BlockPos, ? extends TileEntity> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        charMap.clear();
    }

    @Nonnull
    @Override
    public Set<BlockPos> keySet() {
        return new DelegateKeySet();
    }

    @Nonnull
    @Override
    public Collection<TileEntity> values() {
        return charMap.values();
    }

    @Override
    public Set<Entry<BlockPos, TileEntity>> entrySet() {
        return new DelegateEntrySet();
    }

    private class DelegateKeySet extends AbstractSet<BlockPos> {
        public DelegateKeySet() {}

        public final int size()                 { return FastTileMap.this.size(); }
        public final void clear()               { FastTileMap.this.clear(); }
        public final Iterator<BlockPos> iterator() {
            return isEmpty() ? Collections.emptyIterator() : new DelegateKeyItr();
        }
        public final boolean contains(Object o) {
            return FastTileMap.this.containsKey(o);
        }
        public final boolean remove(Object o) {
            return FastTileMap.this.remove(o) != null;
        }

    }

    private class DelegateEntrySet extends AbstractSet<Entry<BlockPos, TileEntity>> {
        public DelegateEntrySet() {

        }

        public final int size() { return FastTileMap.this.size(); }

        public final void clear() { FastTileMap.this.clear(); }

        @Nonnull
        public final Iterator<Entry<BlockPos, TileEntity>> iterator() {
            return isEmpty() ? Collections.emptyIterator() : new DelegateEntryItr();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object key = e.getKey();
            TileEntity comp = FastTileMap.this.get(key);
            return comp == ((Map.Entry<?, ?>) o).getValue();
        }

        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                MyHashMap.Entry<?, ?> e = (MyHashMap.Entry<?, ?>) o;
                return FastTileMap.this.remove(e.k) != null;
            }
            return false;
        }

        public final Spliterator<Entry<BlockPos, TileEntity>> spliterator() {
            return Spliterators.spliterator(iterator(), size(), 0);
        }

    }

    private class DelegateEntryItr extends AbstractIterator<Entry<BlockPos, TileEntity>> {
        final Iterator<CharMap.Entry<TileEntity>> subItr = FastTileMap.this.charMap.selfEntrySet().iterator();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final FakeEntry fakeEntry = new FakeEntry(pos);
        @Override
        public boolean computeNext() {
            boolean flag = subItr.hasNext();
            if(flag) {
                CharMap.Entry<TileEntity> e = subItr.next();

                fakeEntry.entry = e;

                int i = e.getChar();
                pos.setPos(x + (i >> 12), i & 255, z + (i >> 8) & 15);
                result = fakeEntry;
            }
            return flag;
        }
    }

    private static class FakeEntry implements Map.Entry<BlockPos, TileEntity> {
        CharMap.Entry<TileEntity> entry;
        final BlockPos.MutableBlockPos pos;

        private FakeEntry(BlockPos.MutableBlockPos pos) {this.pos = pos;}

        @Override
        public BlockPos getKey() {
            return pos;
        }

        @Override
        public TileEntity getValue() {
            return entry.getValue();
        }

        @Override
        public TileEntity setValue(TileEntity value) {
            return entry.setValue(value);
        }
    }

    private class DelegateKeyItr extends AbstractIterator<BlockPos> {
        final PrimitiveIterator.OfInt subItr = FastTileMap.this.charMap.selfKeySet().iterator();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        @Override
        public boolean computeNext() {
            boolean flag = subItr.hasNext();
            if(flag) {
                int i = subItr.nextInt();
                result = pos.setPos(x + (i >> 12), i & 255, z + (i >> 8) & 15);
            }
            return flag;
        }
    }
}
