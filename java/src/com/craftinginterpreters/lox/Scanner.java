package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while(!isAtEnd()) { // isn't at end
        // beginning of next lexeme
        start = current;
        scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c =  advance();
    switch(c) {
      case '(': addToken(LEFT_PAREN);  break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE);  break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA);       break;
      case '.': addToken(DOT);         break;
      case '-': addToken(MINUS);       break;
      case '+': addToken(PLUS);        break;
      case ';': addToken(SEMICOLON);   break;
      case '*': addToken(STAR);        break;

      case '!': addToken(match('=') ? BANG_EQUAL   : BANG);    break;
      case '=': addToken(match('=') ? EQUAL_EQUAL  : EQUAL);   break;
      case '<': addToken(match('=') ? LESS_EQUAL   : LESS);    break;
      case '>': addToken(match('=') ? GREATER_EQUAL: GREATER); break;

      case '/':
        if (match('/')) {
          // comment goes until end of line
          while(peek() != '\n' && !isAtEnd())
            advance();
        } else {
          addToken(SLASH);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        break;  //ignore whitespace

      case '\n':
        line++;
        break;

      case '"': string(); break;

      default:
        Lox.error(line, "Unexpected character.");
        break;
    }
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  /*
   * consumes next char in source and returns it
   */
  private char advance() {
    return source.charAt(current++);
  }

  /*
   * lookahead
   * consumes char if condition met
   */
  private boolean match(char expected) {
    if (isAtEnd())
      return false;

    if (source.charAt(current) != expected)
      return false;

    current++;  // consume
    return true;
  }

  /*
   * lookahead - only looks at current consumed char
   * doesn't consume character
   */
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  private void string() {
    while(peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++; // multiline string
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "unterminated string.");
      return;
    }

    advance(); // closing "

    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
}
