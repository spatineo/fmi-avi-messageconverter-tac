package fi.fmi.avi.converter.tac;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.tac.lexer.Lexeme;
import fi.fmi.avi.converter.tac.lexer.SerializingException;
import fi.fmi.avi.model.AviationWeatherMessage;
import fi.fmi.avi.model.metar.METAR;

/**
 * Created by rinne on 06/03/2018.
 */
public class METARTACSerializer extends METARTACSerializerBase<METAR> {

    @Override
    protected Class<METAR> getMessageClass() {
        return METAR.class;
    }

    @Override
    protected Lexeme.Identity getStartTokenIdentity() {
        return Lexeme.Identity.METAR_START;
    }

    @Override
    protected METAR narrow(final AviationWeatherMessage msg, final ConversionHints hints) throws SerializingException {
        if (msg instanceof METAR) {
            return (METAR) msg;
        } else {
            throw new SerializingException("Message to serialize is not a METAR message POJO!");
        }
    }
}
