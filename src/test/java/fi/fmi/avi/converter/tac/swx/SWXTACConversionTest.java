package fi.fmi.avi.converter.tac.swx;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.unitils.thirdparty.org.apache.commons.io.IOUtils;

import fi.fmi.avi.converter.AviMessageConverter;
import fi.fmi.avi.converter.ConversionHints;
import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.converter.tac.TACTestConfiguration;
import fi.fmi.avi.converter.tac.conf.TACConverter;
import fi.fmi.avi.model.PolygonGeometry;
import fi.fmi.avi.model.swx.SpaceWeatherAdvisory;
import fi.fmi.avi.model.swx.SpaceWeatherAdvisoryAnalysis;
import fi.fmi.avi.model.swx.SpaceWeatherRegion;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TACTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class SWXTACConversionTest {
    @Autowired
    private AviMessageConverter converter;

    @Test
    public void parseAndSerialize() throws Exception {
        String input = getInput("spacewx-A2-4.tac");

        final ConversionResult<SpaceWeatherAdvisory> parseResult = this.converter.convertMessage(input, TACConverter.TAC_TO_SWX_POJO);
        assertEquals(0, parseResult.getConversionIssues().size());
        assertEquals(ConversionResult.Status.SUCCESS, parseResult.getStatus());
        assertTrue(parseResult.getConvertedMessage().isPresent());

        SpaceWeatherAdvisory msg = parseResult.getConvertedMessage().get();

        Assert.assertEquals("DONLON", msg.getIssuingCenter().getName().get());
        Assert.assertEquals(2, msg.getAdvisoryNumber().getSerialNumber());
        Assert.assertEquals(2016, msg.getAdvisoryNumber().getYear());
        Assert.assertEquals(1, msg.getReplaceAdvisoryNumber().get().getSerialNumber());
        Assert.assertEquals(2016, msg.getReplaceAdvisoryNumber().get().getYear());
        Assert.assertEquals("RADIATION MOD", msg.getPhenomena().get(0).asCombinedCode());

        Assert.assertEquals(5, msg.getAnalyses().size());
        Assert.assertEquals(SpaceWeatherAdvisoryAnalysis.Type.OBSERVATION, msg.getAnalyses().get(0).getAnalysisType().get());

        ConversionResult<String> SerializeResult = this.converter.convertMessage(msg, TACConverter.SWX_POJO_TO_TAC, new ConversionHints());
        Assert.assertTrue(SerializeResult.getConvertedMessage().isPresent());

        //Assert.assertEquals(input.replace("\n", "\r\n").trim().getBytes(), SerializeResult.getConvertedMessage().get().trim().getBytes());
    }

    @Test
    public void compareParsedObjects() throws Exception {
        String input = getInput("spacewx-pecasus-mnhmsh.tac");
        ConversionHints hints = new ConversionHints();
        hints.put(ConversionHints.KEY_SWX_LABEL_END_LENGTH, 19);

        final ConversionResult<SpaceWeatherAdvisory> parseResult = this.converter.convertMessage(input, TACConverter.TAC_TO_SWX_POJO);
        assertEquals(0, parseResult.getConversionIssues().size());
        assertEquals(ConversionResult.Status.SUCCESS, parseResult.getStatus());
        assertTrue(parseResult.getConvertedMessage().isPresent());

        SpaceWeatherAdvisory msg = parseResult.getConvertedMessage().get();

        Assert.assertEquals("PECASUS", msg.getIssuingCenter().getName().get());
        Assert.assertEquals(9, msg.getAdvisoryNumber().getSerialNumber());
        Assert.assertEquals(2020, msg.getAdvisoryNumber().getYear());
        Assert.assertEquals(8, msg.getReplaceAdvisoryNumber().get().getSerialNumber());
        Assert.assertEquals(2020, msg.getReplaceAdvisoryNumber().get().getYear());
        Assert.assertEquals("HF COM MOD", msg.getPhenomena().get(0).asCombinedCode());
        Assert.assertEquals(5, msg.getAnalyses().size());
        SpaceWeatherAdvisoryAnalysis analysis = msg.getAnalyses().get(0);
        Assert.assertEquals(SpaceWeatherAdvisoryAnalysis.Type.OBSERVATION, analysis.getAnalysisType().get());
        Assert.assertEquals("MNH", analysis.getRegion().get().get(0).getLocationIndicator().get().getCode());
        Assert.assertEquals("MSH", analysis.getRegion().get().get(1).getLocationIndicator().get().getCode());
        Assert.assertEquals("EQN", analysis.getRegion().get().get(2).getLocationIndicator().get().getCode());

        analysis = msg.getAnalyses().get(1);
        Assert.assertTrue(analysis.isNoInformationAvailable());

        ConversionResult<String> SerializeResult = this.converter.convertMessage(msg, TACConverter.SWX_POJO_TO_TAC, hints);
        Assert.assertTrue(SerializeResult.getConvertedMessage().isPresent());

        final ConversionResult<SpaceWeatherAdvisory> reparseResult = this.converter.convertMessage(SerializeResult.getConvertedMessage().get(),
                TACConverter.TAC_TO_SWX_POJO);

        SpaceWeatherAdvisory adv1 = parseResult.getConvertedMessage().get();
        SpaceWeatherAdvisory adv2 = parseResult.getConvertedMessage().get();

        Assert.assertEquals(adv1.getIssuingCenter().getName(), adv2.getIssuingCenter().getName());
        Assert.assertEquals(adv1.getRemarks().get(), adv2.getRemarks().get());
        Assert.assertEquals(adv1.getReplaceAdvisoryNumber().get().getSerialNumber(), adv2.getReplaceAdvisoryNumber().get().getSerialNumber());
        Assert.assertEquals(adv1.getReplaceAdvisoryNumber().get().getYear(), adv2.getReplaceAdvisoryNumber().get().getYear());


        Assert.assertEquals(adv1.getNextAdvisory().getTimeSpecifier(), adv2.getNextAdvisory().getTimeSpecifier());
        Assert.assertEquals(adv1.getNextAdvisory().getTime().get(), adv2.getNextAdvisory().getTime().get());

        Assert.assertEquals(adv1.getPhenomena(), adv2.getPhenomena());
        Assert.assertEquals(adv1.getTranslatedTAC().get(), adv2.getTranslatedTAC().get());

        for(int i = 0; i < adv1.getAnalyses().size(); i++) {
            SpaceWeatherAdvisoryAnalysis analysis1 = adv1.getAnalyses().get(i);
            SpaceWeatherAdvisoryAnalysis analysis2 = adv2.getAnalyses().get(i);

            Assert.assertEquals(analysis1.getAnalysisType().get(), analysis2.getAnalysisType().get());
            Assert.assertEquals(analysis1.getTime(), analysis2.getTime());
            Assert.assertEquals(analysis1.isNoInformationAvailable(), analysis2.isNoInformationAvailable());
            Assert.assertEquals(analysis1.isNoPhenomenaExpected(), analysis2.isNoPhenomenaExpected());
            Assert.assertEquals(analysis1.getRegion().isPresent(), analysis2.getRegion().isPresent());
            if(analysis1.getRegion().isPresent()) {
                for (int a = 0; a < analysis1.getRegion().get().size(); a++) {
                    SpaceWeatherRegion region1 = analysis1.getRegion().get().get(a);
                    SpaceWeatherRegion region2 = analysis2.getRegion().get().get(a);

                    Assert.assertEquals(region1.getLocationIndicator().get(), region2.getLocationIndicator().get());

                    PolygonGeometry geo1 = (PolygonGeometry) region1.getAirSpaceVolume().get().getHorizontalProjection().get();
                    PolygonGeometry geo2 = (PolygonGeometry) region2.getAirSpaceVolume().get().getHorizontalProjection().get();

                    Assert.assertEquals(geo1.getSrsDimension(), geo2.getSrsDimension());
                    Assert.assertEquals(geo1.getSrsName().get(), geo2.getSrsName().get());
                    for (int b = 0; b < geo1.getExteriorRingPositions().size(); b++) {
                        Assert.assertEquals(geo1.getExteriorRingPositions().get(b), geo2.getExteriorRingPositions().get(b));
                    }
                }
            }
        }
    }

    private String getInput(String fileName) throws IOException {
        InputStream is = null;
        try {
            is = SWXReconstructorTest.class.getResourceAsStream(fileName);
            Objects.requireNonNull(is);
            return IOUtils.toString(is, "UTF-8");
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
