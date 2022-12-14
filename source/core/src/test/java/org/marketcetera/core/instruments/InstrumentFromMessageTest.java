package org.marketcetera.core.instruments;

import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.marketcetera.core.LoggerConfiguration;
import org.marketcetera.quickfix.FIXDataDictionaryManager;
import org.marketcetera.quickfix.FIXVersion;
import org.marketcetera.module.ExpectedFailure;
import org.marketcetera.trade.Equity;
import org.marketcetera.trade.Option;
import org.marketcetera.trade.OptionType;
import org.marketcetera.trade.Instrument;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import quickfix.*;
import quickfix.field.*;

import java.math.BigDecimal;

/* $License$ */
/**
 * Tests {@link InstrumentFromMessage} and its subclasses.
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 2.0.0
 */
@ClassVersion("$Id$")
public class InstrumentFromMessageTest {
    
    @BeforeClass
    public static void logSetup() throws Exception {
        LoggerConfiguration.logSetup();
        FIXDataDictionaryManager.initialize(FIX_VERSION,
                FIX_VERSION.getDataDictionaryURL());
    }
    
    @Test
    public void unhandledType() throws Exception {
        Message msg = FIX_VERSION.getMessageFactory().newBasicOrder();
        //unknown security type
        msg.setField(new SecurityType(SecurityType.BANK_NOTES));
        verifyNoHandlerFailure(msg);
        //null CFI code
        msg.setField(new CFICode());
        verifyNoHandlerFailure(msg);
        //empty CFI code
        msg.setField(new CFICode(""));
        verifyNoHandlerFailure(msg);
        //Invalid CFI code
        msg.setField(new CFICode("S"));
        verifyNoHandlerFailure(msg);
        //Invalid CFI code
        msg.setField(new CFICode("SO"));
        verifyNoHandlerFailure(msg);
        //remove security type field and test
        msg.removeField(SecurityType.FIELD);
        verifyNoHandlerFailure(msg);
    }
    
    @Test
    public void equity() throws Exception {
        //no security type, no symbol
        Message msg = FIX_VERSION.getMessageFactory().newBasicOrder();
        assertEquals(false, msg.isSetField(SecurityType.FIELD));
        assertNull(InstrumentFromMessage.SELECTOR.forValue(msg).extract(msg));
        // no security type, symbol set
        msg.setField(new Symbol("PQR"));
        assertEquals(new Equity("PQR"), InstrumentFromMessage.SELECTOR.forValue(msg).extract(msg));
        //security type set, symbol set
        msg.setField(new SecurityType(SecurityType.COMMON_STOCK));
        assertEquals(new Equity("PQR"), InstrumentFromMessage.SELECTOR.forValue(msg).extract(msg));
    }

    @Test
    public void option() throws Exception {
        String expectedSymbol = "PQR";
        String expectedMY = "201010";
        String expectedDay = "10";
        String expectedExpiry = expectedMY + expectedDay;
        BigDecimal expectedStrike = BigDecimal.TEN;
        //Test all permutations of fields.
        for (Field secType : new Field[]{null, new SecurityType(SecurityType.OPTION), new CFICode("O")}) {
            for (Field symbol : new Field[]{null, new Symbol(expectedSymbol)}) {
                for (Field optType : new Field[]{null, new PutOrCall(), new PutOrCall(PutOrCall.CALL), new PutOrCall(PutOrCall.PUT), new CFICode("O"), new CFICode("OC"), new CFICode("OP")}) {
                    for (Field strikePrice : new Field[]{null, new StrikePrice(expectedStrike)}) {
                        for (Field expiry : new Field[]{null, new MaturityMonthYear(expectedMY), new MaturityDate(expectedExpiry)}) {
                            for(Field expiryDay: new Field[]{null, new MaturityDay(expectedDay)}) {
                                Message m = FIX_VERSION.getMessageFactory().newBasicOrder();
                                setFields(m, secType, symbol, optType, strikePrice, expiry, expiryDay);
                                SLF4JLoggerProxy.debug(this,"{},{},{},{},{},{}",secType,symbol,optType,strikePrice,expiry,expiryDay);
                                //figure out if we expect a value.
                                boolean isNotOption = (secType == null && (optType == null || (!(optType instanceof CFICode)))) ||
                                        symbol == null ||
                                        optType == null ||
                                        (optType instanceof CFICode && ((String)optType.getObject()).length() < 2) ||
                                        strikePrice == null ||
                                        expiry == null;
                                Instrument instrument = InstrumentFromMessage.SELECTOR.forValue(m).extract(m);
                                if(isNotOption) {
                                    assertThat(instrument, anyOf(nullValue(), not(instanceOf(Option.class))));
                                } else {
                                    Option option = (Option) instrument;
                                    assertEquals(org.marketcetera.trade.SecurityType.Option, option.getSecurityType());
                                    assertEquals(expectedSymbol, option.getSymbol());
                                    assertEquals(expectedStrike, option.getStrikePrice());
                                    if (expiry.getTag() == MaturityMonthYear.FIELD) {
                                        assertEquals(expiryDay == null? expectedMY: expectedExpiry, option.getExpiry());
                                    } else {
                                        assertEquals(expectedExpiry, option.getExpiry());
                                    }
                                    OptionType oType;
                                    if(optType.getTag() == PutOrCall.FIELD) {
                                        oType = OptionType.getInstanceForFIXValue((Integer) optType.getObject());
                                    } else {
                                        oType = CFICodeUtils.getOptionType((String)optType.getObject());
                                    }
                                    assertEquals(oType, option.getType());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void setFields(Message inMessage, Field... inFields) {
        for(Field field:inFields) {
            if(field != null) {
                if (field instanceof StringField) {
                    inMessage.setField((StringField) field);
                } else if(field instanceof IntField) {
                    inMessage.setField((IntField) field);
                } else if(field instanceof DecimalField) {
                    inMessage.setField((DecimalField) field);
                } else {
                    fail("unhandled field:" + field);
                }
            }
        }
    }

    private void verifyNoHandlerFailure(final Message inMsg) throws Exception {
        new ExpectedFailure<IllegalArgumentException>(
                Messages.NO_HANDLER_FOR_VALUE.getText(inMsg,
                        InstrumentFromMessage.class.getName())){
            @Override
            protected void run() throws Exception {
                InstrumentFromMessage.SELECTOR.forValue(inMsg);
            }
        };
    }

    private static final FIXVersion FIX_VERSION = FIXVersion.FIX44;
}
