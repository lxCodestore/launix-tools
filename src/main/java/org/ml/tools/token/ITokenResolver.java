/*
 * Taken from https://github.com/jjenkov/TokenReplacingReader - no license specified there
 */
package org.ml.tools.token;

/**
 *
 */
public interface ITokenResolver {

    /**
     *
     * @param tokenName
     * @return
     */
    String resolveToken(String tokenName);
}
