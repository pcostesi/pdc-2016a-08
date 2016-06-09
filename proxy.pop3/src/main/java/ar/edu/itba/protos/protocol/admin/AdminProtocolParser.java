package ar.edu.itba.protos.protocol.admin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminProtocolParser {

    private static enum ParserState {
        IN_TOKEN, WHITESPACE, CR, LF, ERR
    }

    private ParserState state = ParserState.WHITESPACE;
    private List<String> pendingTokens = new ArrayList<>();

    /**
     * Ugh. This is ugly. If I had time, I would do a StateMachineBuilder class
     * that would build a StateMachine which could abstract this.
     */
    // TODO(@pcostesi) Extract to StateMachine + Builder
    public List<String> tokenize(final ByteBuffer buffer) {
        final List<String> tokens = new ArrayList<>();
        int tokenStart = buffer.position();
        int position = tokenStart;

        for (position = buffer.position(); position < buffer.limit(); position++) {
            final char current = (char) buffer.get(position);
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
                        tokens.add(new String(Arrays.copyOfRange(buffer.array(), tokenStart, position)));
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
        buffer.position(tokenStart);
        buffer.compact();
        return tokens;
    }

    /**
     * Parse a ByteBuffer and return a Command with all the tokens filled in or
     * empty if we still need more information.
     *
     * @param buffer
     * @return
     */
    public List<String[]> parse(final ByteBuffer buffer) {
        pendingTokens.addAll(tokenize(buffer));
        final List<String[]> commands = new ArrayList<>();
        int crlf = 0;
        while (crlf != -1) {
            crlf = pendingTokens.indexOf(null);
            if (crlf == -1) {
                break;
            } else if (crlf > 0) {
                commands.add(pendingTokens.subList(0, crlf).toArray(new String[] {}));
            }
            // TODO(@pcostesi) OPTIMIZE!
            pendingTokens = new ArrayList<String>(pendingTokens.subList(crlf + 1, pendingTokens.size()));
        }
        return commands;
    }
}
