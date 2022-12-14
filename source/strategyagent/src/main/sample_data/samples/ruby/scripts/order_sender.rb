#
# $License$
#
# author:anshul@marketcetera.com
# since 1.0.0
# version: $Id$
#
#
'require java'
include_class "org.marketcetera.strategy.ruby.Strategy"
include_class "org.marketcetera.trade.Factory"
include_class "org.marketcetera.trade.OrderType"
include_class "org.marketcetera.trade.Side"
include_class "org.marketcetera.trade.TimeInForce"
include_class "org.marketcetera.trade.Equity"
include_class "org.marketcetera.marketdata.MarketDataRequest"
include_class "java.math.BigDecimal"

###############################
# Order Sender Strategy       #
###############################
class OrderSender < Strategy
    SYMBOLS = "AMZN" # Depends on MD - can be other symbols
    MARKET_DATA_PROVIDER = "marketcetera" # Can be activ, bogus, marketcetera
    ACCOUNT = "accountable"

    ##########################################
    # Executed when the strategy is started. #
    #                                        #
    # Use this method to set up data flows   #
    #  and other initialization tasks.       #
    ##########################################
    def on_start
      request = MarketDataRequest.newRequest().withSymbols(SYMBOLS).fromProvider(MARKET_DATA_PROVIDER)
      @requestID=request_market_data(request)
      info "Issued Market Data Request " + @requestID.to_s
      @receivedData = false
    end

    ###################################################
    # Executed when the strategy receives a bid event #
    ###################################################
    def on_bid(bid)
      if @receivedData then
        return
      end
      @receivedData = true

      info "Bid: " + bid.to_s
      # Send an order to buy and cancel the request
      order = Factory.instance.createOrderSingle()
      order.setAccount ACCOUNT
      order.setOrderType OrderType::Limit
      order.setPrice bid.price
      order.setQuantity bid.size
      order.setSide Side::Buy
      order.setInstrument bid.instrument
      order.setTimeInForce TimeInForce::Day
      warn "Sending Order " + order.to_s

      send order
      warn "Sent Order:"+order.to_s

      cancel_data_request @requestID
      info "Cancelled Market Data Request " + @requestID.to_s
    end

    ####################################################
    # Executed when the strategy receives an ask event #
    ####################################################
    def on_ask(ask)
      if @receivedData then
        return
      end
      @receivedData = true

      info "Ask: " + ask.to_s
      # Send an order to sell and cancel the request
      order = Factory.instance.createOrderSingle()
      order.setAccount ACCOUNT
      order.setOrderType OrderType::Limit
      order.setPrice ask.price
      order.setQuantity ask.size
      order.setSide Side::Sell
      order.setInstrument ask.instrument
      order.setTimeInForce TimeInForce::Day
      warn "Sending Order " + order.to_s

      send order
      warn "Sent Order:"+order.to_s

      cancel_data_request @requestID
      info "Cancelled Market Data Request " + @requestID.to_s
    end

    ###########################################################
    # Executed when the strategy receives an execution report #
    ###########################################################
    def on_execution_report(executionReport)
      warn "Received Execution Report:" + executionReport.to_s
    end
end
