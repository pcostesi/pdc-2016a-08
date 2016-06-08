package ar.edu.itba.protos.protocol.admin.parser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminProtocolParser {

    private static enum ParserState {
        IN_TOKEN, WHITESPACE, CR, LF, ERR
    }

    private ParserState state = ParserState.WHITESPACE;
    private final List<String> pendingTokens = new ArrayList<>();

    /**
     * Ugh. This is ugly. If I had time, I would do a StateMachineBuilder class
     * that would build a StateMachine which could abstract this.
     */
    public List<String> tokenize(final ByteBuffer buffer) {
        final CharBuffer cb = StandardCharsets.UTF_8.decode(buffer);
        final List<String> tokens = new ArrayList<>();
        int tokenStart = cb.position();

        for (int position = cb.position(); position < cb.remaining(); position++) {
            final char current = cb.get(position);

            switch (state) {
                case WHITESPACE:
                    tokenStart = position;
                    if (current == '\r') {
                        state = ParserState.CR;
                    } else if (current == '\n') {
                        state = ParserState.LF;
                        tokens.add(null);
                    } else if (!Character.isWhitespace(current)) {
                        state = ParserState.IN_TOKEN;
                    }
                    break;

                case IN_TOKEN:
                    if (current == '\r' || current == '\n' || Character.isWhitespace(current)) {
                        tokens.add(String.copyValueOf(cb.array(), tokenStart, position - tokenStart));
                        tokenStart = position;

                        if (current == '\r') {
                            state = ParserState.CR;
                        } else if (current == '\n') {
                            state = ParserState.LF;
                            tokens.add(null);
                        } else {
                            state = ParserState.WHITESPACE;
                        }
                    }
                    break;

                case CR:
                    if (current == '\r') {
                        state = ParserState.CR;
                    } else if (current == '\n') {
                        state = ParserState.LF;
                        tokens.add(null);
                    } else {
                        state = ParserState.ERR;
                    }
                    break;

                case LF:
                    if (!Character.isWhitespace(current)) {
                        state = ParserState.IN_TOKEN;
                        tokenStart = position;
                    } else {
                        state = ParserState.WHITESPACE;
                    }
                    break;

                default:
                    throw new RuntimeException("Error parsing");
            }

        }
        return tokens;
    }

    /**
     * Parse a ByteBuffer and return a Command with all the tokens filled in or
     * empty if we still need more information.
     *
     * @param buffer
     * @return
     */
    public Optional<? super Command> parse(final ByteBuffer buffer) {
        pendingTokens.addAll(tokenize(buffer));
        return Optional.empty();
    }
}
