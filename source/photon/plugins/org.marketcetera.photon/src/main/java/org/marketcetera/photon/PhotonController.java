package org.marketcetera.photon;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.WorkbenchException;
import org.marketcetera.client.Client;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.client.ClientManager;
import org.marketcetera.client.ConnectionException;
import org.marketcetera.client.OrderValidationException;
import org.marketcetera.client.ReportListener;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.core.NoMoreIDsException;
import org.marketcetera.event.HasFIXMessage;
import org.marketcetera.messagehistory.MessageVisitor;
import org.marketcetera.messagehistory.ReportHolder;
import org.marketcetera.messagehistory.TradeReportsHistory;
import org.marketcetera.quickfix.FIXMessageUtil;
import org.marketcetera.quickfix.MarketceteraFIXException;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.FIXOrder;
import org.marketcetera.trade.Factory;
import org.marketcetera.trade.Instrument;
import org.marketcetera.trade.Order;
import org.marketcetera.trade.OrderCancel;
import org.marketcetera.trade.OrderCancelReject;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderReplace;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.OrderStatus;
import org.marketcetera.trade.ReportBase;
import org.marketcetera.util.except.I18NException;
import org.marketcetera.util.log.I18NBoundMessage1P;

import quickfix.FieldNotFound;

/* $License$ */

/**
 * OrderManager is the main repository for business logic.  It can be considered
 * the "controller" in a standard model-view-controller architecture.  The main entry
 * points are the <code>handle*</code> methods for handling incoming and outgoing 
 * messages of various types.  
 * 
 * @author gmiller
 * @version $Id$
 * @since 1.0.0
 */
@ClassVersion("$Id$")
public class PhotonController
    implements Messages, ReportListener
{

	private Logger internalMainLogger = PhotonPlugin.getMainConsoleLogger();

	private TradeReportsHistory fixMessageHistory;

	public static final BrokerID DEFAULT_BROKER = null; 

	public void setMessageHistory(TradeReportsHistory fixMessageHistory) 
	{
		this.fixMessageHistory = fixMessageHistory;
	}
	
	@Override
	public void receiveCancelReject(OrderCancelReject inReport) {
		checkReportID(inReport);
		fixMessageHistory.addIncomingMessage(inReport);
		try {
			handleCancelReject(inReport);
		} catch (FieldNotFound e) {
			MarketceteraFIXException mfix = MarketceteraFIXException.createFieldNotFoundException(e);
			internalMainLogger.error(CANNOT_DECODE_INCOMING_SPECIFIED_MESSAGE.getText(mfix.getMessage()),
			                         mfix);
		}		
	}

	@Override
	public void receiveExecutionReport(ExecutionReport inReport) {
		checkReportID(inReport);
		fixMessageHistory.addIncomingMessage(inReport);
		try {
			handleExecutionReport(inReport);
		} catch (NoMoreIDsException e) {
            internalMainLogger.error(CANNOT_DECODE_INCOMING_MESSAGE.getText(),
                    e);
		} catch (FieldNotFound e) {
			MarketceteraFIXException mfix = MarketceteraFIXException.createFieldNotFoundException(e);
			internalMainLogger.error(CANNOT_DECODE_INCOMING_SPECIFIED_MESSAGE.getText(mfix.getMessage()),
			                         mfix);
		}		
	}
	
	private void checkReportID(ReportBase report) {
		if (report.getReportID() == null) {
			internalMainLogger.error(PHOTON_CONTROLLER_MISSING_REPORT_ID.getText(report.toString()));
		}
	}
	
	protected void asyncExec(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	protected void handleExecutionReport(ExecutionReport inReport) throws FieldNotFound, NoMoreIDsException {

		if (OrderStatus.Rejected == inReport.getOrderStatus()) {
			// TODO: improve ExecutionReport API to expose encoded text
			String rejectReason = inReport.getText();
			if(rejectReason == null) {
				if (inReport instanceof HasFIXMessage) {
					rejectReason = FIXMessageUtil.getTextOrEncodedText(((HasFIXMessage) inReport).getMessage(), Messages.UNKNOWN_VALUE.getText());
				} else {
					rejectReason = Messages.UNKNOWN_VALUE.getText();
				}
			}
			
			org.marketcetera.trade.OrderID orderID = inReport.getOrderID();
			
			Instrument instrument = inReport.getInstrument();
			String rejectMsg = REJECT_MESSAGE.getText(orderID.getValue(),
			                                          instrument == null ? Messages.UNKNOWN_VALUE.getText() : instrument.getSymbol(),
			                                          rejectReason);
			internalMainLogger.error(rejectMsg);
		}
	}


	protected void handleCancelReject(OrderCancelReject inReport) throws FieldNotFound {
		String reason = null;
		String text = inReport.getText();
		if(text == null) {
			text = Messages.UNKNOWN_VALUE.getText();
		}
		String origClOrdID = Messages.UNKNOWN_VALUE.getText();
		org.marketcetera.trade.OrderID oID = inReport.getOriginalOrderID();
		if(oID != null) {
			origClOrdID = oID.getValue();
		}
		String errorMsg = CANCEL_REJECT_MESSAGE.getText(origClOrdID,
		                                                (text == null ? 0 : 1),
		                                                text,
		                                                (reason == null ? 0 : 1),
		                                                reason);
		internalMainLogger.error(errorMsg);
	}
	
	public void cancelOneOrderByClOrdID(String clOrdID) throws NoMoreIDsException {
		OrderID orderid = new OrderID(clOrdID);
		ExecutionReport report = getReportForCancel(orderid);
		if (report != null) {
			if (internalMainLogger.isDebugEnabled()) {
				internalMainLogger
						.debug("Exec id for cancel execution report:" + report.getExecutionID()); //$NON-NLS-1$
			}
			OrderCancel cancel = Factory.getInstance().createOrderCancel(report);
            /*
             * Remove the broker order id since some of our reports have "NONE"
             * which is an invalid value.
             */
            cancel.setBrokerOrderID(null);
			sendOrder(cancel);
		} else {
			internalMainLogger.error(CANNOT_SEND_CANCEL.getText(clOrdID));
			return;
		}
	}

	private ExecutionReport getReportForCancel(OrderID orderid) {
		ReportHolder firstReportHolder = fixMessageHistory.getFirstReport(orderid);
		ExecutionReport report;
		if (firstReportHolder != null) {
			report = (ExecutionReport) firstReportHolder.getReport();
		} else {
			report = fixMessageHistory.getLatestExecutionReport(orderid);
		}
		return report;
	}
	
	public void replaceOrder(ExecutionReport report) throws WorkbenchException {
        ExecutionReport originalReport = getReportForCancel(report.getOrderID());
        if (originalReport != null) {
            OrderReplace replace = Factory.getInstance().createOrderReplace(
                    originalReport);
            /*
             * Remove the broker order id since some of our reports have "NONE"
             * which is an invalid value.
             */
            replace.setBrokerOrderID(null);
            PhotonPlugin.getDefault().showOrderInTicket(replace);
        }
	}

	/** Panic button: cancel all open orders
	 * Need to do the cancel in 2 phases: first collect all clOrderIds to cancel, 
	 * then cancel them.
	 * Trying to cancel them while collecting results in a deadlock, since we are 
	 * holding a read lock while collecting, and sending a cancel tries to acquire 
	 * the write lock to add new messages to message history. 
	 * @param monitor progress monitor
	 */
	public void cancelAllOpenOrders(IProgressMonitor monitor) {
		final Vector<String> clOrdIdsToCancel = new Vector<String>();
		fixMessageHistory.visitOpenOrdersExecutionReports(new MessageVisitor() {
			public void visitOpenOrderExecutionReports(ReportBase report) {
				String clOrdId = report.getOrderID().getValue();
				if (clOrdId == null) {
					internalMainLogger.error(CANNOT_SEND_CANCEL_FOR_REASON
							.getText(clOrdId, report));
				} else {
					clOrdIdsToCancel.add(clOrdId);
				}
			}
		});
		monitor.beginTask(Messages.PHOTON_CONTROLLER_CANCEL_ALL_ORDERS_TASK
				.getText(), clOrdIdsToCancel.size());
		for (String clOrdId : clOrdIdsToCancel) {
			try {
				cancelOneOrderByClOrdID(clOrdId);
				if (internalMainLogger.isDebugEnabled()) {
					internalMainLogger.debug("cancelling order for " + clOrdId);} //$NON-NLS-1$
				monitor.worked(1);
			} catch (NoMoreIDsException ignored) {
				// ignore
			}
		}
		monitor.done();
	}

	public void sendOrder(Order inOrder) {
		internalMainLogger.info(PHOTON_CONTROLLER_SENDING_MESSAGE.getText(inOrder.toString()));
		if (ClientManager.isInitialized()){
			try {
				Client client = ClientManager.getInstance();
				if(inOrder instanceof OrderSingle) {
					client.sendOrder((OrderSingle) inOrder);
				} else if(inOrder instanceof OrderReplace) {
					client.sendOrder((OrderReplace)inOrder);
				} else if(inOrder instanceof OrderCancel) {
					client.sendOrder((OrderCancel)inOrder);
				} else if(inOrder instanceof FIXOrder) {
					client.sendOrderRaw((FIXOrder)inOrder);
				} else {
					internalMainLogger.error(SEND_ORDER_FAIL_UNKNOWN_TYPE.getText(inOrder.toString()));
				}
			} catch (OrderValidationException e){
				internalMainLogger.error(SEND_ORDER_VALIDATION_FAILED.getText(inOrder.toString()), e);
			} catch (ConnectionException ignore){
				//ignore as the exception listener will be invoked 
			} catch (ClientInitException e) {
				internalMainLogger.error(SEND_ORDER_NOT_INITIALIZED.getText(inOrder.toString()), e);
			}
		} else {
			internalMainLogger.error(CANNOT_SEND_NOT_CONNECTED.getText());
		}
	}

    public void sendOrderChecked(Order inOrder) throws I18NException {
        internalMainLogger.info(PHOTON_CONTROLLER_SENDING_MESSAGE
                .getText(inOrder.toString()));
        try {
            Client client = ClientManager.getInstance();
            if (inOrder instanceof OrderSingle) {
                client.sendOrder((OrderSingle) inOrder);
            } else if (inOrder instanceof OrderReplace) {
                client.sendOrder((OrderReplace) inOrder);
            } else if (inOrder instanceof OrderCancel) {
                client.sendOrder((OrderCancel) inOrder);
            } else if (inOrder instanceof FIXOrder) {
                client.sendOrderRaw((FIXOrder) inOrder);
            } else {
                throw new I18NException(new I18NBoundMessage1P(
                        SEND_ORDER_FAIL_UNKNOWN_TYPE, inOrder.toString()));
            }
        } catch (OrderValidationException e) {
            throw new I18NException(new I18NBoundMessage1P(
                    SEND_ORDER_VALIDATION_FAILED, inOrder.toString()));
        } catch (ClientInitException e) {
            throw new I18NException(CANNOT_SEND_NOT_CONNECTED);
        } catch (ConnectionException e) {
            throw new I18NException(CANNOT_SEND_NOT_CONNECTED);
        }
    }
	
	/**
	 * @return Returns the mainConsoleLogger.
	 */
	public Logger getMainConsoleLogger() {
		return internalMainLogger;
	}

	/**
	 * @param mainConsoleLogger The mainConsoleLogger to set.
	 */
	public void setMainConsoleLogger(Logger mainConsoleLogger) {
		this.internalMainLogger = mainConsoleLogger;
	}


}
