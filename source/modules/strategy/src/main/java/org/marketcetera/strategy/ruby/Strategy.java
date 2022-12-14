package org.marketcetera.strategy.ruby;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.marketcetera.client.brokers.BrokerStatus;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.core.notifications.Notification;
import org.marketcetera.core.position.PositionKey;
import org.marketcetera.event.AskEvent;
import org.marketcetera.event.BidEvent;
import org.marketcetera.event.DividendEvent;
import org.marketcetera.event.Event;
import org.marketcetera.event.MarketstatEvent;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.module.DataFlowID;
import org.marketcetera.module.DataFlowSupport;
import org.marketcetera.module.DataRequest;
import org.marketcetera.module.ModuleURN;
import org.marketcetera.strategy.AbstractRunningStrategy;
import org.marketcetera.strategy.RunningStrategy;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.Equity;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.Option;
import org.marketcetera.trade.OptionType;
import org.marketcetera.trade.OrderCancel;
import org.marketcetera.trade.OrderCancelReject;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderReplace;
import org.marketcetera.trade.OrderSingle;

import quickfix.Message;

/* $License$ */

/**
 * {@link RunningStrategy} implementation for Ruby strategies to extend.
 * 
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since 1.0.0
 */
@ClassVersion("$Id$")
public class Strategy
        extends AbstractRunningStrategy
{
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.IStrategy#onAsk(org.marketcetera.event.AskEvent)
     */
    @Override
    public final void onAsk(AskEvent inAsk)
    {
        on_ask(inAsk);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.IStrategy#onBid(org.marketcetera.event.BidEvent)
     */
    @Override
    public final void onBid(BidEvent inBid)
    {
        on_bid(inBid);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.strategy.RunningStrategy#onMarketstat(org.marketcetera.event.MarketstatEvent)
     */
    @Override
    public void onMarketstat(MarketstatEvent inStatistics)
    {
        on_marketstat(inStatistics);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.strategy.RunningStrategy#onDividend(org.marketcetera.event.DividendEvent)
     */
    @Override
    public void onDividend(DividendEvent inDividend)
    {
        on_dividend(inDividend);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.IStrategy#onCallback()
     */
    @Override
    public final void onCallback(Object inData)
    {
        on_callback(inData);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.IStrategy#onExecutionReport(org.marketcetera.event.ExecutionReport)
     */
    @Override
    public final void onExecutionReport(ExecutionReport inExecutionReport)
    {
        on_execution_report(inExecutionReport);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.strategy.RunningStrategy#onCancel(org.marketcetera.trade.OrderCancelReject)
     */
    @Override
    public final void onCancelReject(OrderCancelReject inCancel)
    {
        on_cancel_reject(inCancel);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.IStrategy#onTrade(org.marketcetera.event.TradeEvent)
     */
    @Override
    public final void onTrade(TradeEvent inTrade)
    {
        on_trade(inTrade);
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.marketcetera.strategy.AbstractStrategy#onOther(java.lang.Object)
     */
    @Override
    public final void onOther(Object inEvent)
    {
        on_other(inEvent);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.strategy.RunningStrategy#onStart()
     */
    @Override
    public final void onStart()
    {
        on_start();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.strategy.RunningStrategy#onStop()
     */
    @Override
    public final void onStop()
    {
        on_stop();
    }
    // callbacks that Ruby scripts may override
    /**
     * Invoked when the <code>Strategy</code> receives an {@link AskEvent}.
     * 
     * @param inAsk an <code>AskEvent</code> value
     */
    protected void on_ask(AskEvent inAsk)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives a {@link BidEvent}.
     * 
     * @param inBid a <code>BidEvent</code> value
     */
    protected void on_bid(BidEvent inBid)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives a {@link MarketstatEvent}.
     * 
     * @param inStatistics a <code>MarketstatEvent</code> value
     */
    protected void on_marketstat(MarketstatEvent inStatistics)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives a {@link DividendEvent}.
     * 
     * @param inStatistics a <code>DividendEvent</code> value
     */
    protected void on_dividend(DividendEvent inStatistics)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives an {@link ExecutionReport}.
     * 
     * @param inExecutionReport an <code>ExecutionReport</code> value
     */
    protected void on_execution_report(ExecutionReport inExecutionReport)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives an {@link OrderCancelReject}.
     * 
     * @param inCancelReject an <code>OrderCancelReject</code> value
     */
    protected void on_cancel_reject(OrderCancelReject inCancelReject)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives a {@link TradeEvent}.
     * 
     * @param inTrade a <code>TradeEvent</code> value
     */
    protected void on_trade(TradeEvent inTrade)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives an object that does not fit any of the other categories.
     * 
     * @param inEvent an <code>Object</code> value
     */
    protected void on_other(Object inEvent)
    {
    }
    /**
     * Invoked when the <code>Strategy</code> receives a callback requested via {@link #request_callback_at(Date, Object)}
     * or {@link #request_callback_after(long, Object)}.
     * 
     * @param inData an <code>Object</code> value which was passed to the request, may be null
     */
    protected void on_callback(Object inData)
    {
    }
    /**
     * Called when a strategy is started.
     */
    protected void on_start()
    {
    }
    /**
     * Called when a strategy is about to be stopped.
     */
    protected void on_stop()
    {
    }
    // services provided to Ruby scripts
    /**
     * Sets the given key to the given value in the storage area common to all running strategies.
     * 
     * @param inKey a <code>String</code> value
     * @param inValue a <code>String</code> value
     */
    public final void set_property(String inKey,
                                   String inValue)
    {
        setProperty(inKey,
                    inValue);
    }
    /**
     * Gets the parameter associated with the given name.
     * 
     * @param inName a <code>String</code> value containing the key of a parameter key/value value
     * @return a <code>String</code> value or null if no parameter is associated with the given name
     */
    public final String get_parameter(String inName)
    {
        return getParameter(inName);
    }
    /**
     * Gets the property associated with the given name.
     * 
     * @param inName a <code>String</code> value containing the key of a property key/value value
     * @return a <code>String</code> value or null if no property is associated with the given name
     */
    public final String get_property(String inName)
    {
        return getProperty(inName);
    }
    /**
     * Requests a callback after a specified delay in milliseconds.
     *
     * <p>The callback will be executed as close to the specified millisecond
     * as possible.  There is no guarantee that the timing will be exact.  If
     * more than one callback is requested by the same {@link RunningStrategy}
     * for the same millisecond, the requests will be processed serially in
     * FIFO order.  This implies that a long-running callback request may
     * delay other callbacks from the same {@link RunningStrategy} unless the
     * caller takes steps to mitigate the bottleneck.
     *
     * @param inDelay a <code>long</code> value indicating how many milliseconds
     *   to wait before executing the callback.  A value <= 0 will be interpreted
     *   as a request for an immediate callback.
     * @param inData an <code>Object</code> value to deliver along with the callback,
     *   may be null
     */
    public final void request_callback_after(long inDelay,
                                             Object inData)
    {
        requestCallbackAfter(inDelay,
                             inData);
    }
    /**
     * Requests a callback at a specific point in time.
     *
     * <p>The callback will be executed as close to the specified millisecond
     * as possible.  There is no guarantee that the timing will be exact.  If
     * more than one callback is requested by the same {@link RunningStrategy}
     * for the same millisecond, the requests will be processed serially in
     * FIFO order.  This implies that a long-running callback request may
     * delay other callbacks from the same {@link RunningStrategy} unless the
     * caller takes steps to mitigate the bottleneck.
     *
     * @param inDate a <code>Date</code> value at which to execute the callback.  A date
     *   value earlier than the present will be interpreted as a request for an
     *   immediate callback.
     * @param inData an <code>Object</code> value to deliver with the callback or null
     */
    public final void request_callback_at(Date inDate,
                                          Object inData)
    {
        requestCallbackAt(inDate,
                          inData);
    }
    /**
     * Creates a market data request.
     * 
     * <p>The <code>inRequest</code> object must refer to a started market data provider module.
     * 
     * @param inRequest a <code>MarketDataRequest</code> value containing the request to execute
     * @return an <code>int</code> value containing an identifier corresponding to this market data request or 0 if the request failed
     */
    public final int request_market_data(MarketDataRequest inRequest)
    {
        return requestMarketData(inRequest);
    }
    /**
     * Creates a market data request.
     * 
     * <p>The <code>inRequest</code> object must refer to a started market data provider module.
     * 
     * @param inRequest a <code>String</code> value containing the representation of a {@link MarketDataRequest} to execute
     * @return an <code>int</code> value containing an identifier corresponding to this market data request or 0 if the request failed
     */
    public final int request_market_data(String inRequest)
    {
        return requestMarketData(inRequest);
    }
    /**
     * Requests market data processed by the given complex event processor from the given source.
     *
     * @param inRequest a <code>MarketDataRequest</code> value containing the request to execute
     * @param inStatements a <code>String[]</code> value containing the statements to pass to the
     *   complex event processor.  The meaning of the statements varies according to the actual
     *   event processor that handles them.
     * @param inCepSource a <code>String</code> value containing the name of the complex event processor
     *   to which to send the query request
     * @return an <code>int</code> value containing the handle of the request or 0 if the request failed
     */
    public final int request_processed_market_data(MarketDataRequest inRequest,
                                                   String[] inStatements,
                                                   String inCepSource)
    {
        return requestProcessedMarketData(inRequest,
                                          inStatements,
                                          inCepSource);
    }
    /**
     * Requests market data processed by the given complex event processor from the given source.
     *
     * @param inRequest a <code>String</code> value containing the representation of a {@link MarketDataRequest} to execute
     * @param inStatements a <code>String[]</code> value containing the statements to pass to the
     *   complex event processor.  The meaning of the statements varies according to the actual
     *   event processor that handles them.
     * @param inCepSource a <code>String</code> value containing the name of the complex event processor
     *   to which to send the query request
     * @return an <code>int</code> value containing the handle of the request or 0 if the request failed
     */
    public final int request_processed_market_data(String inRequest,
                                                   String[] inStatements,
                                                   String inCepSource)
    {
        return requestProcessedMarketData(inRequest,
                                          inStatements,
                                          inCepSource);
    }
    /**
     * Cancels a given data request.
     * 
     * <p>If the given <code>inRequestID</code> identifier does not correspond to an active data
     * request, this method does nothing.
     *
     * @param inRequestID an <code>int</code> value identifying the data request to cancel
     */
    public final void cancel_data_request(int inRequestID)
    {
        cancelDataRequest(inRequestID);
    }
    /**
     * Cancels all data requests for this {@link Strategy}.
     *
     * <p>If there are no active data requests for this {@link Strategy}, this method does nothing.
     */
    public final void cancel_all_data_requests()
    {
        cancelAllDataRequests();
    }
    /**
     * Creates a complex event processor request.
     * 
     * @param inStatements a <code>String[]</code> value containing an array of statements that comprises the request
     * @param inSource a <code>String</code> value indicating what market data provider from which to request the data
     * @return an <code>int</code> value containing an identifier corresponding to this market data request or 0 if the request failed
     */
    public final int request_cep_data(String[] inStatements,
                                      String inSource)
    {
        return requestCEPData(inStatements,
                              inSource);
    }
    /**
     * Suggests a trade.
     *
     * @param inOrder an <code>OrderSingle</code> value containing the trade to suggest
     * @param inScore a <code>BigDecimal</code> value containing the score of this suggestion.  this value is determined by the user
     *   but is recommended to fit in the interval [0..1]
     * @param inIdentifier a <code>String</code> value containing a user-specified string to identify the suggestion
     */
    public final void suggest_trade(OrderSingle inOrder,
                                    BigDecimal inScore,
                                    String inIdentifier)
    {
        suggestTrade(inOrder,
                     inScore,
                     inIdentifier);
    }
    /**
     * Sends a <code>FIX</code> message to all subscribers to which orders are sent.
     *
     * @param inMessage a <code>Message</code> value
     * @param inBroker a <code>BrokerID</code> value
     */
    public final void send_message(Message inMessage,
                                   BrokerID inBroker)
    {
        sendMessage(inMessage,
                    inBroker);
    }
    /**
     * Sends the given event to the CEP module indicated by the provider.
     * 
     * <p>The corresponding CEP module must already exist or the message will not be sent.
     *
     * @param inEvent an <code>Event</code> value containing the event to be sent
     * @param inProvider a <code>String</code> value containing the name of a CEP provider
     */
    public final void send_event_to_cep(Event inEvent,
                                        String inProvider)
    {
        sendEventToCEP(inEvent,
                       inProvider);
    }
    /**
     * Sends the given event to the appropriate subscribers. 
     *
     * @param inEvent an <code>Event</code> value
     */
    public final void send_event(Event inEvent)
    {
        sendEvent(inEvent);
    }
    /**
     * Sends an order to order subscribers.
     * 
     * <p><code>OrderSingle</code> objects passed to this method will be added to the list of submitted orders
     * but other object types will not.  In order to track, for example, <code>OrderReplace</code> and <code>OrderCancel</code>
     * objects, they must have first been created via {@link #cancel_replace(OrderID, OrderSingle, boolean)} and
     * {@link #cancel_order(OrderID, boolean)} respectively. 
     * 
     * @param inData an <code>Object</code> value
     * @return a <code>boolean</code> value indicating whether the object was successfully transmitted or not
     */
    public final boolean send(Object inData)
    {
        return super.send(inData);
    }
    /**
     * Submits a request to cancel the <code>OrderSingle</code> with the given <code>OrderID</code>.
     * 
     * <p>The order must have been submitted by this strategy during this session or this call will
     * have no effect.
     *
     * @param inOrderID an <code>OrderID</code> value
     * @param inSendOrder a <code>boolean</code> value indicating whether the <code>OrderCancel</code> should be submitted or just returned to the caller.  If <code>false</code>,
     *   it is the caller's responsibility to submit the <code>OrderReplace</code> with {@link #send(Object)}.
     * @return an <code>OrderCancel</code> value containing the cancel order or <code>null</code> if
     *   the <code>OrderCancel</code> could not be constructed
     */
    public final OrderCancel cancel_order(OrderID inOrderID,
                                          boolean inSendOrder)
    {
        return cancelOrder(inOrderID,
                           inSendOrder);
    }
    /**
     * Submits a cancel-replace order for the given <code>OrderID</code> with the given <code>Order</code>. 
     *
     * <p>The order must have been submitted by this strategy during this session or this call will
     * have no effect.  If <code>inSendOrder</code> is <code>false</code>, it is the caller's responsibility to submit the <code>OrderReplace</code>.
     *
     * @param inOrderID an <code>OrderID</code> value containing the order to cancel
     * @param inNewOrder an <code>OrderSingle</code> value containing the order with which to replace the existing order
     * @param inSendOrder a <code>boolean</code> value indicating whether the <code>OrderReplace</code> should be submitted or just returned to the caller.  If <code>false</code>,
     *   it is the caller's responsibility to submit the <code>OrderReplace</code> with {@link #send(Object)}.
     * @return an <code>OrderReplace</code> value containing the new order or <code>null</code> if the old order could not be canceled and the new one could not be sent
     */
    public final OrderReplace cancel_replace(OrderID inOrderID,
                                             OrderSingle inNewOrder,
                                             boolean inSendOrder)
    {
        return cancelReplace(inOrderID,
                             inNewOrder,
                             inSendOrder);
    }
    /**
     * Submits cancel requests for all <code>OrderSingle</code> objects created during this session.
     * 
     * <p>This method will make a best-effort attempt to cancel all orders.  If an attempt to cancel one order
     * fails, that order will be skipped and the others will still be attempted in their turn.
     * @return an <code>int</code> value containing the number of orders to cancel
     */
    public final int cancel_all_orders()
    {
        return cancelAllOrders();
    }
    /**
     * Initiates a data flow request.
     * 
     * <p>See {@link DataFlowSupport#createDataFlow(DataRequest[], boolean)}. 
     *
     * @param inAppendDataSink a <code>boolean</code> value indicating if the sink module should be appended to the
     *   data pipeline, if it's not already requested as the last module and the last module is capable of emitting data.
     * @param inRequests a <code>DataRequest[]</code> value containing the ordered list of requests. Each instance
     *   identifies a stage of the data pipeline. The data from the first stage is piped to the next.
     * @return a <code>DataFlowID</code> value containing a unique ID identifying the data flow. The ID can be used to cancel
     *   the data flow request and get more details on it.  Returns <code>null</code> if the request could not be created.
     */
    public final DataFlowID create_data_flow(boolean inAppendDataSink,
                                             DataRequest[] inRequests)
    {
        return createDataFlow(inAppendDataSink,
                              inRequests);
    }
    /**
     * Cancels a data flow identified by the supplied data flow ID.
     *
     * <p>See {@link DataFlowSupport#cancel(DataFlowID)}.
     *
     * @param inDataFlowID a <code>DataFlowID</code> value containing the request handle that was returned from
     *   a prior call to {@link #create_data_flow(boolean, DataRequest[])}
     */
    public final void cancel_data_flow(DataFlowID inDataFlowID)
    {
        cancelDataFlow(inDataFlowID);
    }
    /**
     * Gets the <code>ExecutionReport</code> values received during the current session that
     * match the given <code>OrderID</code>.
     * 
     * <p>Note that the <code>OrderID</code> must match an <code>OrderSingle</code> generated
     * by this strategy during the current session.  If not, an empty list will be returned.
     *
     * @param inOrderID an <code>OrderID</code> value corresponding to an <code>OrderSingle</code>
     *   generated during this session by this strategy via {@link #send(Object)} or {@link #cancel_replace(OrderID, OrderSingle, boolean)}
     * @return an <code>ExecutionReport[]</code> value containing the <code>ExecutionReport</code> objects as limited according to
     *   the conditions enumerated above
     */
    public final ExecutionReport[] get_execution_reports(OrderID inOrderID)
    {
        return getExecutionReports(inOrderID);
    }
    /**
     * Creates and issues a {@link Notification} at low priority.
     *
     * @param inSubject a <code>String</code> value
     * @param inBody a <code>String</code> value
     */
    public final void notify_low(String inSubject,
                                 String inBody)
    {
        sendNotification(Notification.low(inSubject,
                                          inBody,
                                          this.toString()));
    }
    /**
     * Creates and issues a {@link Notification} at medium priority.
     *
     * @param inSubject a <code>String</code> value
     * @param inBody a <code>String</code> value
     */
    public final void notify_medium(String inSubject,
                                    String inBody)
    {
        sendNotification(Notification.medium(inSubject,
                                             inBody,
                                             this.toString()));
    }
    /**
     * Creates and issues a {@link Notification} at high priority.
     *
     * @param inSubject a <code>String</code> value
     * @param inBody a <code>String</code> value
     */
    public final void notify_high(String inSubject,
                                  String inBody)
    {
        sendNotification(Notification.high(inSubject,
                                           inBody,
                                           this.toString()));
    }
    /**
     * Emits the given debug message to the strategy log output.
     *
     * @param inMessage a <code>String</code> value
     */
    @Override
    public final void debug(String inMessage)
    {
        super.debug(inMessage);
    }
    /**
     * Emits the given info message to the strategy log output.
     *
     * @param inMessage a <code>String</code> value
     */
    @Override
    public final void info(String inMessage)
    {
        super.info(inMessage);
    }
    /**
     * Emits the given warn message to the strategy log output.
     *
     * @param inMessage a <code>String</code> value
     */
    @Override
    public final void warn(String inMessage)
    {
        super.warn(inMessage);
    }
    /**
     * Emits the given error message to the strategy log output.
     *
     * @param inMessage a <code>String</code> value
     */
    @Override
    public final void error(String inMessage)
    {
        super.error(inMessage);
    }
    /**
     * Returns the list of brokers known to the system.
     *
     * <p>These values can be used to create and send orders with {@link #send_message(Message, BrokerID)}
     * or {@link #send(Object)}.
     *
     * @return a <code>BrokerStatus[]</code> value
     */
    public final BrokerStatus[] get_brokers()
    {
        return getBrokers();
    }
    /**
     * Gets the position in the given <code>Equity</code> at the given point in time.
     *
     * <p>Note that this method will not retrieve <code>Option</code> positions.  To retrieve
     * <code>Option</code> positions, use {@link #get_option_position_as_of(Date, String, String, BigDecimal, OptionType)}. 
     * 
     * @param inDate a <code>Date</code> value indicating the point in time for which to search
     * @param inSymbol a <code>String</code> value containing the <code>Equity</code> symbol
     * @return a <code>BigDecimal</code> value or <code>null</code> if no position could be found 
     */
    public final BigDecimal get_position_as_of(Date inDate,
                                               String inSymbol)
    {
        return getPositionAsOf(inDate,
                               inSymbol);
    }
    /**
     * Gets all open <code>Equity</code> positions at the given point in time.
     *
     * @param inDate a <code>Date</code> value indicating the point in time for which to search
     * @return a <code>Map&lt;PositionKey&lt;Equity&gt;,BigDecimal&gt;</code> value
     */
    public final Map<PositionKey<Equity>,BigDecimal> get_all_positions_as_of(Date inDate)
    {
        return getAllPositionsAsOf(inDate);
    }
    /**
     * Gets the position in the given <code>Option</code> at the given point in time.
     *
     * @param inDate a <code>Date</code> value indicating the point in time for which to search
     * @param inOptionRoot a <code>String</code> value
     * @param inExpiry a <code>String</code> value
     * @param inStrikePrice a <code>BigDecimal</code> value
     * @param inOptionType an <code>OptionType</code> value
     * @return a <code>BigDecimal</code> value or <code>null</code> if no position could be found
     */
    public final BigDecimal get_option_position_as_of(Date inDate,
                                                      String inOptionRoot,
                                                      String inExpiry,
                                                      BigDecimal inStrikePrice,
                                                      OptionType inType)
    {
        return getOptionPositionAsOf(inDate,
                                     inOptionRoot,
                                     inExpiry,
                                     inStrikePrice,
                                     inType);
    }
    /**
     * Gets all open <code>Option</code> positions at the given point in time.
     *
     * @param inDate a <code>Date</code> value indicating the point in time for which to search
     * @return a <code>Map&lt;PositionKey&lt;Option&gt;,BigDecimal&gt;</code> value
     */
    public final Map<PositionKey<Option>,BigDecimal> get_all_option_positions_as_of(Date inDate)
    {
        return getAllOptionPositionsAsOf(inDate);
    }
    /**
     * Gets open positions for the options specified by the given option roots at the given point in time. 
     *
     * @param inDate a <code>Date</code> value indicating the point in time for which to search
     * @param inOptionRoots a <code>String[]</code> value containing the specific option roots for which to search
     * @return a <code>Map&lt;PositionKey&lt;Option&gt;,BigDecimal&gt;</code> value
     */
    public final Map<PositionKey<Option>,BigDecimal> get_option_positions_as_of(Date inDate,
                                                                                String[] inOptionRoots)
    {
        return getOptionPositionsAsOf(inDate,
                                      inOptionRoots);
    }
    /**
     * Gets the underlying symbol for the given option root, if available.
     *
     * @param inOptionRoot a <code>String</code> value containing an option root
     * @return a <code>String</code> value containing the symbol for the underlying instrument or <code>null</code> if
     *  no underlying instrument could be found
     */
    public final String get_underlying(String inOptionRoot)
    {
        return getUnderlying(inOptionRoot);
    }
    /**
     * Gets the set of of known option roots for the given underlying symbol. 
     *
     * @param inUnderlying a <code>String</code> value containing the symbol of an underlying instrument
     * @return a <code>Collection&lt;String&gt;</code> value sorted lexicographically by option root or <code>null</code>
     *  if no option roots could be found
     */
    public final Collection<String> get_option_roots(String inUnderlying)
    {
        return getOptionRoots(inUnderlying);
    }
    /**
     * Gets the {@link ModuleURN} of this strategy.
     *
     * @return a <code>ModuleURN</code> value
     */
    public final ModuleURN get_urn()
    {
        return getURN();
    }
}
