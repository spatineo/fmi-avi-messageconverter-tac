package fi.fmi.avi.converter.tac.lexer.impl.token;

import static fi.fmi.avi.converter.tac.lexer.Lexeme.ParsedValueName.HOUR2;
import static fi.fmi.avi.converter.tac.lexer.Lexeme.ParsedValueName.MINUTE2;

import java.util.regex.Matcher;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.tac.lexer.Lexeme;
import fi.fmi.avi.converter.tac.lexer.LexemeIdentity;

public class USSigmetValidUntil extends TimeHandlingRegex {

    public USSigmetValidUntil(final OccurrenceFrequency prio) {
        super("^VALID\\s+UNTIL\\s+(?<endHour>[0-9]{2})(?<endMinute>[0-9]{2})Z$", prio);
    }

    @Override
    public void visitIfMatched(final Lexeme token, final Matcher match, final ConversionHints hints) {
        final int toHour = Integer.parseInt(match.group("endHour"));
        final int toMinute = Integer.parseInt(match.group("endMinute"));
        if (timeOkHourMinute(toHour, toMinute)) {
            token.identify(LexemeIdentity.VALID_TIME);
            token.setParsedValue(HOUR2, toHour);
            token.setParsedValue(MINUTE2, toMinute);
        } else {
            token.identify(LexemeIdentity.VALID_TIME, Lexeme.Status.SYNTAX_ERROR, "Invalid time");
        }
    }
}
