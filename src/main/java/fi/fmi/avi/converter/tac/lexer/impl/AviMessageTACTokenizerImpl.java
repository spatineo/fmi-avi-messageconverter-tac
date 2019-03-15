package fi.fmi.avi.converter.tac.lexer.impl;

import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.tac.bulletin.TAFBulletinTACSerializer;
import fi.fmi.avi.converter.tac.lexer.AviMessageTACTokenizer;
import fi.fmi.avi.converter.tac.lexer.LexemeSequence;
import fi.fmi.avi.converter.tac.lexer.SerializingException;
import fi.fmi.avi.converter.tac.metar.METARTACSerializer;
import fi.fmi.avi.converter.tac.metar.SPECITACSerializer;
import fi.fmi.avi.converter.tac.taf.TAFTACSerializer;
import fi.fmi.avi.model.AviationWeatherMessageOrCollection;
import fi.fmi.avi.model.metar.METAR;
import fi.fmi.avi.model.metar.SPECI;
import fi.fmi.avi.model.taf.TAF;
import fi.fmi.avi.model.taf.TAFBulletin;

public class AviMessageTACTokenizerImpl implements AviMessageTACTokenizer {
	private METARTACSerializer metarSerializer;
    private SPECITACSerializer speciSerializer;
    private TAFTACSerializer tafSerializer;
    private TAFBulletinTACSerializer tafBulletinSerializer;

	public void setMETARSerializer(METARTACSerializer serializer) {
		this.metarSerializer = serializer;
	}

    public void setSPECISerializer(SPECITACSerializer serializer) {
        this.speciSerializer = serializer;
    }

	public void setTAFSerializer(TAFTACSerializer serializer) {
		this.tafSerializer = serializer;
	}

    public void setTAFBUlletinSerializer(TAFBulletinTACSerializer serializer) {
        this.tafBulletinSerializer = serializer;
    }

	public AviMessageTACTokenizerImpl() {
	}

	@Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessageOrCollection msg) throws SerializingException {
		return this.tokenizeMessage(msg, null);
	}

	@Override
    public LexemeSequence tokenizeMessage(final AviationWeatherMessageOrCollection msg, final ConversionHints hints) throws SerializingException {
        if (msg instanceof SPECI && this.speciSerializer != null) {
            return this.speciSerializer.tokenizeMessage(msg, hints);
		} else if (msg instanceof METAR && this.metarSerializer != null) {
			return this.metarSerializer.tokenizeMessage(msg, hints);
        } else if (msg instanceof TAF && this.tafSerializer != null) {
            return this.tafSerializer.tokenizeMessage(msg, hints);
        } else if (msg instanceof TAFBulletin && this.tafBulletinSerializer != null) {
            return this.tafBulletinSerializer.tokenizeMessage(msg, hints);
        }
		throw new IllegalArgumentException("Do not know how to tokenize message of type " + msg.getClass().getCanonicalName());
	}

}
