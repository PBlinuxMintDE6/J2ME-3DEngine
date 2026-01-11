/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package J3DE;

/**
 *
 * @author c
 */
public class Token {

    static final int IDENT = 0;
    static final int NUMBER = 1;
    static final int STRING = 2;
    static final int SYMBOL = 3;

    int type;
    String text;

    Token(int type, String text) {
        this.type = type;
        this.text = text;
    }
}
