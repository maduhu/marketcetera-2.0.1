package org.marketcetera.ors.history;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.marketcetera.ors.Principals;
import org.marketcetera.ors.security.SimpleUser;
import org.marketcetera.persist.PersistenceException;
import org.marketcetera.trade.*;
import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.core.position.PositionKey;

/* $License$ */
/**
 * Provides services to save and query reports.
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 1.0.0
 */
@ClassVersion("$Id$")
public class ReportHistoryServices {
    /**
     * Returns all the reports received after the supplied date-time
     * value, and which are visible to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date-time value. Cannot be null.
     *
     * @return the reports that were received after the date-time
     * value, and which are visible to the given user.
     *
     * @throws PersistenceException if there were persistence errors
     * fetching the reports.
     * @throws ReportPersistenceException if the data retrieved had
     * unexpected errors.
     */
    public ReportBaseImpl[] getReportsSince
        (SimpleUser inUser,
         Date inDate)
            throws PersistenceException, ReportPersistenceException {
        MultiPersistentReportQuery query = MultiPersistentReportQuery.all();
        query.setSendingTimeAfterFilter(inDate);
        if (!inUser.isSuperuser()) {
            query.setViewerFilter(inUser);
        }
        query.setEntityOrder(MultiPersistentReportQuery.BY_ID);

        List<PersistentReport> reportList = query.fetch();
        ReportBaseImpl [] reports = new ReportBaseImpl[reportList.size()];
        int i = 0;
        for(PersistentReport report: reportList) {
            reports[i++] = (ReportBaseImpl) report.toReport();
        }
        return reports;
    }

    /**
     * Returns the position of the equity based on all reports
     * received for it before or on the supplied date, and which are visible
     * to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only the reports
     * that were received prior to or on this date will be used in this calculation.
     * Cannot be null.
     *
     * @param inEquity the equity whose position is desired. Cannot be null.
     *
     * @return the equity position.
     *
     * @throws PersistenceException if there were errors retrieving the equity
     * position
     */
    public BigDecimal getEquityPositionAsOf
        (SimpleUser inUser,
         Date inDate,
         Equity inEquity)
        throws PersistenceException
    {
        return ExecutionReportSummary.getEquityPositionAsOf
            (inUser,inDate,inEquity);
    }
    /**
     * Returns the aggregate position of each (equity,account,actor)
     * tuple based on all reports received for each tuple on or before
     * the supplied date, and which are visible to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Equity>, BigDecimal> getAllEquityPositionsAsOf
        (SimpleUser inUser,
         Date inDate)
        throws PersistenceException
    {
        return ExecutionReportSummary.getAllEquityPositionsAsOf(inUser,inDate);
    }

    /**
     * Gets the current aggregate position for the option instrument based on
     * execution reports received before or on the supplied date, and which
     * are visible to the given user.
     *
     * <p>
     * Buy trades result in positive positions. All other kinds of trades
     * result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the time. execution reports with sending time values less
     * than or equal to this time are included in this calculation.
     * @param inOption The option instrument
     *
     * @return the aggregate position for the symbol.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position.
     */
    public BigDecimal getOptionPositionAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final Option inOption)
        throws PersistenceException {
        return ExecutionReportSummary.getOptionPositionAsOf(inUser,
                inDate, inOption);
    }

    /**
     * Returns the aggregate position of each option
     * (option,account,actor)
     * tuple based on all reports received for each option instrument on or before
     * the supplied date, and which are visible to the given user.
     *
     * <p> Buy trades result in positive positions. All other kinds of
     * trades result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Option>, BigDecimal> getAllOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate)
        throws PersistenceException {
        return ExecutionReportSummary.getAllOptionPositionsAsOf(inUser, inDate);
    }

    /**
     * Returns the aggregate position of each option
     * (option,account,actor)
     * tuple based on all reports received for each option instrument on or before
     * the supplied date, and which are visible to the given user.
     *
     * <p> Buy trades result in positive positions. All other kinds of
     * trades result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     * @param inSymbols the list of option roots.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Option>, BigDecimal> getOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final String... inSymbols)
        throws PersistenceException {
        return ExecutionReportSummary.getOptionPositionsAsOf(inUser, inDate, inSymbols);
    }

    /**
     * Saves the supplied report to the database.
     *
     * @param inReport the report to be saved. Cannot be null.
     *
     * @throws org.marketcetera.persist.PersistenceException if there
     * were errors saving the report.
     */
    public void save(ReportBase inReport) throws PersistenceException {
        PersistentReport.save(inReport);
    }

    /**
     * Returns the principals associated with the report with given
     * order ID.
     *
     * @param orderID The order ID.
     *
     * @return The principals. If no report with the given order ID
     * exists, {@link Principals#UNKNOWN} is returned, and no
     * exception is thrown.
     *
     * @throws PersistenceException Thrown if there were errors
     * accessing the report.
     */

    public Principals getPrincipals
        (OrderID orderID)
        throws PersistenceException
    {
        return PersistentReport.getPrincipals(orderID);
    }
}
