/*
 * Copyright 2016, Jerry Xiong, BOSSCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bosscs.spark.commons.rdd;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper class holding information of a computed token range.
 */
public class TokenRange<T, K> implements Comparable<TokenRange>, Serializable {
    private final T startToken;
    private final T endToken;
    private List<K> replicas;

    /**
     * Construct a new token range with no replica information.
     * 
     * @param startToken
     *            first token of this range.
     * @param endToken
     *            last token of this range.
     */
    public TokenRange(T startToken, T endToken) {
        this.startToken = startToken;
        this.endToken = endToken;
    }

    /**
     * Construct a new token range with replica information.
     * 
     * @param startToken
     *            first token of this range.
     * @param endToken
     *            last token of this range.
     * @param replicas
     *            the list of replica machines holding this range of tokens.
     */
    public TokenRange(T startToken, T endToken, List<K> replicas) {
        this.startToken = startToken;
        this.endToken = endToken;
        this.replicas = replicas;
    }

    public TokenRange(K[] replicas, T startToken, T endToken) {
        this.startToken = startToken;
        this.endToken = endToken;
        this.replicas = Arrays.asList(replicas);
    }

    /**
     * Construct a new token range with replica information.
     * 
     * @param startToken
     *            first token of this range.
     * @param endToken
     *            last token of this range.
     * @param replicas
     *            the list of replica machines holding this range of tokens.
     */
    // public DeepTokenRange(T startToken, T endToken, List<String> replicas) {
    // this.startToken = startToken;
    // this.endToken = endToken;
    // this.replicas = replicas;
    // }

    /**
     * Construct a new token range with replica information
     * 
     * @param replicas
     */
    // public DeepTokenRange(String[] replicas) {
    // this.replicas = Arrays.asList(replicas);
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DeepTokenRange{" +
                "startToken=" + startToken +
                ", endToken=" + endToken +
                ", replicas=" + replicas +
                "}\n";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TokenRange that = (TokenRange) o;

        if (!endToken.equals(that.endToken)) {
            return false;
        }
        if (!startToken.equals(that.startToken)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = startToken.hashCode();
        result = 31 * result + endToken.hashCode();
        return result;
    }

    public T getStartToken() {
        return startToken;
    }

    public T getStartTokenAsComparable() {
        return startToken;
    }

    public T getEndToken() {
        return endToken;
    }

    public T getEndTokenAsComparable() {
        return endToken;
    }

    public List<K> getReplicas() {
        return replicas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(TokenRange o) {
        if (startToken instanceof Comparable) {
            return ((Comparable) startToken).compareTo(o.startToken);
        }
        return 0;
    }

}
