package org.marketcetera.ors.history;

import org.marketcetera.core.position.PositionKey;
import org.marketcetera.core.position.PositionKeyFactory;
import org.marketcetera.ors.security.SimpleUser;
import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.persist.*;
import org.marketcetera.persist.PersistenceException;
import org.marketcetera.trade.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/* $License$ */
/**
 * Maintains a summary of fields of an ExecutionReport
 * to aid Position calculations. The lifecycle of this object
 * is controlled by {@link PersistentReport}
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 1.0.0
 */
@ClassVersion("$Id$")
@Entity
@Table(name="execreports")

@NamedQuery(name = "rootIDForOrderID",
        query = "select e.rootID from ExecutionReportSummary e " +
                "where e.orderID = :orderID")

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "positionForSymbol",
            columns = {@ColumnResult(name = "position")}),
    @SqlResultSetMapping(name = "eqAllPositions",
            columns = {
                @ColumnResult(name = "symbol"),
                @ColumnResult(name = "account"),
                @ColumnResult(name = "actor"),
                @ColumnResult(name = "position")
                    }),
    @SqlResultSetMapping(name = "optAllPositions",
            columns = {
                @ColumnResult(name = "symbol"),
                @ColumnResult(name = "expiry"),
                @ColumnResult(name = "strikePrice"),
                @ColumnResult(name = "optionType"),
                @ColumnResult(name = "account"),
                @ColumnResult(name = "actor"),
                @ColumnResult(name = "position")
                    })
        })

@NamedNativeQueries({
    @NamedNativeQuery(name = "eqPositionForSymbol",query = "select " +
            "sum(case when e.side = :sideBuy then e.cumQuantity else -e.cumQuantity end) as position " +
            "from execreports e " +
            "where e.symbol = :symbol " +
            "and (e.securityType is null " +
            "or e.securityType = :securityType) " +
            "and e.sendingTime <= :sendingTime " +
            "and (:allViewers or e.viewer_id = :viewerID) " +
            "and e.id = " +
            "(select max(s.id) from execreports s where s.rootID = e.rootID)",
            resultSetMapping = "positionForSymbol"),
    @NamedNativeQuery(name = "eqAllPositions",query = "select " +
            "e.symbol as symbol, e.account as account, r.actor_id as actor, sum(case when e.side = :sideBuy then e.cumQuantity else -e.cumQuantity end) as position " +
            "from execreports e " +
            "join reports r on (e.report_id=r.id) " +
            "where e.sendingTime <= :sendingTime " +
            "and (e.securityType is null " +
            "or e.securityType = :securityType) " +
            "and (:allViewers or e.viewer_id = :viewerID) " +
            "and e.id = " +
            "(select max(s.id) from execreports s where s.rootID = e.rootID) " +
            "group by symbol, account, actor having position <> 0",
            resultSetMapping = "eqAllPositions"),
    @NamedNativeQuery(name = "optPositionForTuple",query = "select " +
            "sum(case when e.side = :sideBuy then e.cumQuantity else -e.cumQuantity end) as position " +
            "from execreports e " +
            "where e.symbol = :symbol " +
            "and e.securityType = :securityType " +
            "and e.expiry = :expiry " +
            "and e.strikePrice = :strikePrice " +
            "and e.optionType = :optionType " +
            "and e.sendingTime <= :sendingTime " +
            "and (:allViewers or e.viewer_id = :viewerID) " +
            "and e.id = " +
            "(select max(s.id) from execreports s where s.rootID = e.rootID)",
            resultSetMapping = "positionForSymbol"),
    @NamedNativeQuery(name = "optAllPositions",query = "select " +
            "e.symbol as symbol, e.expiry as expiry, e.strikePrice as strikePrice, e.optionType as optionType, e.account as account, r.actor_id as actor, sum(case when e.side = :sideBuy then e.cumQuantity else -e.cumQuantity end) as position " +
            "from execreports e " +
            "join reports r on (e.report_id=r.id) " +
            "where e.sendingTime <= :sendingTime " +
            "and e.securityType = :securityType " +
            "and (:allViewers or e.viewer_id = :viewerID) " +
            "and e.id = " +
            "(select max(s.id) from execreports s where s.rootID = e.rootID) " +
            "group by symbol, expiry, strikePrice, optionType, account, actor having position <> 0",
            resultSetMapping = "optAllPositions"),
    @NamedNativeQuery(name = "optPositionsForRoots",query = "select " +
            "e.symbol as symbol, e.expiry as expiry, e.strikePrice as strikePrice, e.optionType as optionType, e.account as account, r.actor_id as actor, sum(case when e.side = :sideBuy then e.cumQuantity else -e.cumQuantity end) as position " +
            "from execreports e " +
            "join reports r on (e.report_id=r.id) " +
            "where e.sendingTime <= :sendingTime " +
            "and e.securityType = :securityType " +
            "and e.symbol in (:symbols) " +
            "and (:allViewers or e.viewer_id = :viewerID) " +
            "and e.id = " +
            "(select max(s.id) from execreports s where s.rootID = e.rootID) " +
            "group by symbol, expiry, strikePrice, optionType, account, actor having position <> 0",
            resultSetMapping = "optAllPositions")
        })

class ExecutionReportSummary extends EntityBase {

    /**
     * Gets the current aggregate position for the equity based on
     * execution reports received on or before the supplied time, and which
     * are visible to the given user.
     *
     * <p>
     * Buy trades result in positive positions. All other kinds of trades
     * result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the time. execution reports with sending time values less
     * than or equal to this time are included in this calculation.
     * @param inEquity the equity for which this position needs to be computed
     *
     * @return the aggregate position for the equity.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position.
     */
    static BigDecimal getEquityPositionAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final Equity inEquity)
        throws PersistenceException
    {
        BigDecimal position = executeRemote(new Transaction<BigDecimal>() {
            private static final long serialVersionUID = 1L;

            @Override
            public BigDecimal execute(EntityManager em, PersistContext context) {
                Query query = em.createNamedQuery(
                        "eqPositionForSymbol");  //$NON-NLS-1$

                query.setParameter("viewerID",inUser.getUserID().getValue());  //$NON-NLS-1$
                query.setParameter("allViewers",inUser.isSuperuser());  //$NON-NLS-1$
                query.setParameter("sideBuy", Side.Buy.ordinal());  //$NON-NLS-1$
                query.setParameter("symbol", inEquity.getSymbol());  //$NON-NLS-1$
                query.setParameter("securityType", SecurityType.CommonStock.ordinal());  //$NON-NLS-1$
                query.setParameter("sendingTime", inDate,  //$NON-NLS-1$
                        TemporalType.TIMESTAMP);
                return (BigDecimal) query.getSingleResult();  //$NON-NLS-1$
            }
        }, null);
        return position == null? BigDecimal.ZERO: position;

    }
    /**
     * Returns the aggregate position of each (equity,account,actor)
     * tuple based on all reports received for each tuple on or before
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
    static Map<PositionKey<Equity>, BigDecimal> getAllEquityPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate)
        throws PersistenceException
    {
        return executeRemote(new Transaction<Map<PositionKey<Equity>, BigDecimal>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Map<PositionKey<Equity>, BigDecimal> execute(EntityManager em,
                                                    PersistContext context) {
                Query query = em.createNamedQuery(
                        "eqAllPositions");  //$NON-NLS-1$
                query.setParameter("viewerID",inUser.getUserID().getValue());  //$NON-NLS-1$
                query.setParameter("allViewers",inUser.isSuperuser());  //$NON-NLS-1$
                query.setParameter("sideBuy", Side.Buy.ordinal());  //$NON-NLS-1$
                query.setParameter("securityType", SecurityType.CommonStock.ordinal());  //$NON-NLS-1$
                query.setParameter("sendingTime", inDate,  //$NON-NLS-1$
                        TemporalType.TIMESTAMP);
                HashMap<PositionKey<Equity>, BigDecimal> map =
                        new HashMap<PositionKey<Equity>, BigDecimal>();
                List<?> list = query.getResultList();
                Object[] columns;
                for(Object o: list) {
                    columns = (Object[]) o;
                    //4 columns
                    if(columns.length > 1) {
                        //first one is the symbol
                        //second one is the account
                        //third one is the actor ID
                        //fourth one is the position
                        map.put(PositionKeyFactory.createEquityKey
                                ((String)columns[0],
                                 (String)columns[1],
                                 ((columns[2]==null)?null:
                                  ((BigInteger)columns[2]).toString())),
                                 (BigDecimal)columns[3]);
                    }
                }
                return map;
            }
        }, null);

    }


    /**
     * Gets the current aggregate position for the option tuple based on
     * execution reports received on or before the supplied time, and which
     * are visible to the given user.
     *
     * <p>
     * Buy trades result in positive positions. All other kinds of trades
     * result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the time. execution reports with sending time values less
     * than or equal to this time are included in this calculation.
     * @param inOption option instrument
     *
     * @return the aggregate position for the symbol.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position.
     */
    static BigDecimal getOptionPositionAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final Option inOption)
        throws PersistenceException {
        BigDecimal position = executeRemote(new Transaction<BigDecimal>() {
            private static final long serialVersionUID = 1L;

            @Override
            public BigDecimal execute(EntityManager em, PersistContext context) {
                Query query = em.createNamedQuery(
                        "optPositionForTuple");  //$NON-NLS-1$

                query.setParameter("viewerID",inUser.getUserID().getValue());  //$NON-NLS-1$
                query.setParameter("allViewers",inUser.isSuperuser());  //$NON-NLS-1$
                query.setParameter("sideBuy", Side.Buy.ordinal());  //$NON-NLS-1$
                query.setParameter("symbol", inOption.getSymbol());  //$NON-NLS-1$
                query.setParameter("securityType", SecurityType.Option.ordinal());  //$NON-NLS-1$
                query.setParameter("expiry", inOption.getExpiry());  //$NON-NLS-1$
                query.setParameter("strikePrice", inOption.getStrikePrice());  //$NON-NLS-1$
                query.setParameter("optionType", inOption.getType().ordinal());  //$NON-NLS-1$
                query.setParameter("sendingTime", inDate,  //$NON-NLS-1$
                        TemporalType.TIMESTAMP);
                return (BigDecimal) query.getSingleResult();  //$NON-NLS-1$
            }
        }, null);
        return position == null? BigDecimal.ZERO: position;

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
    static Map<PositionKey<Option>, BigDecimal> getAllOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate)
        throws PersistenceException {
        return executeRemote(new Transaction<Map<PositionKey<Option>, BigDecimal>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Map<PositionKey<Option>, BigDecimal> execute(EntityManager em,
                                                    PersistContext context) {
                Query query = em.createNamedQuery(
                        "optAllPositions");  //$NON-NLS-1$
                query.setParameter("viewerID",inUser.getUserID().getValue());  //$NON-NLS-1$
                query.setParameter("allViewers",inUser.isSuperuser());  //$NON-NLS-1$
                query.setParameter("sideBuy", Side.Buy.ordinal());  //$NON-NLS-1$
                query.setParameter("securityType", SecurityType.Option.ordinal());  //$NON-NLS-1$
                query.setParameter("sendingTime", inDate,  //$NON-NLS-1$
                        TemporalType.TIMESTAMP);
                HashMap<PositionKey<Option>, BigDecimal> map =
                        new HashMap<PositionKey<Option>, BigDecimal>();
                List<?> list = query.getResultList();
                Object[] columns;
                for(Object o: list) {
                    columns = (Object[]) o;
                    //7 columns
                    if(columns.length > 1) {
                        //first one is the symbol
                        //second one is the expiry
                        //third one is the strikePrice
                        //fourth one is the option type
                        //fifth one is the account
                        //sixth one is the actor ID
                        //seventh one is the position
                        map.put(PositionKeyFactory.createOptionKey
                                ((String)columns[0],
                                 (String)columns[1],
                                 (BigDecimal)columns[2],
                                 OptionType.values()[(Integer)columns[3]],
                                 (String)columns[4],
                                 ((columns[5]==null)?null:
                                  ((BigInteger)columns[5]).toString())),
                                 (BigDecimal)columns[6]);
                    }
                }
                return map;
            }
        }, null);

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
     * @param inRootSymbols the list of option roots.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    static Map<PositionKey<Option>, BigDecimal> getOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final String... inRootSymbols)
        throws PersistenceException {
        return executeRemote(new Transaction<Map<PositionKey<Option>, BigDecimal>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Map<PositionKey<Option>, BigDecimal> execute(EntityManager em,
                                                    PersistContext context) {
                Query query = em.createNamedQuery("optPositionsForRoots");  //$NON-NLS-1$
                query.setParameter("viewerID",inUser.getUserID().getValue());  //$NON-NLS-1$
                query.setParameter("allViewers",inUser.isSuperuser());  //$NON-NLS-1$
                query.setParameter("sideBuy", Side.Buy.ordinal());  //$NON-NLS-1$
                query.setParameter("securityType", SecurityType.Option.ordinal());  //$NON-NLS-1$
                query.setParameter("symbols", Arrays.asList(inRootSymbols));  //$NON-NLS-1$
                query.setParameter("sendingTime", inDate,  //$NON-NLS-1$
                        TemporalType.TIMESTAMP);
                HashMap<PositionKey<Option>, BigDecimal> map =
                        new HashMap<PositionKey<Option>, BigDecimal>();
                List<?> list = query.getResultList();
                Object[] columns;
                for(Object o: list) {
                    columns = (Object[]) o;
                    //7 columns
                    if(columns.length > 1) {
                        //first one is the symbol
                        //second one is the expiry
                        //third one is the strikePrice
                        //fourth one is the optionType
                        //fifth one is the account
                        //sixth one is the actor ID
                        //seventh one is the position
                        map.put(PositionKeyFactory.createOptionKey
                                ((String)columns[0],
                                 (String)columns[1],
                                 (BigDecimal)columns[2],
                                 OptionType.values()[(Integer)columns[3]],
                                 (String)columns[4],
                                 ((columns[5]==null)?null:
                                  ((BigInteger)columns[5]).toString())),
                                 (BigDecimal)columns[6]);
                    }
                }
                return map;
            }
        }, null);

    }

    /**
     * Creates an instance.
     *
     * @param inReport The original execution report message.
     * @param inSavedReport the saved persistent report.
     */
    ExecutionReportSummary(ExecutionReport inReport,
                           PersistentReport inSavedReport) {
        setReport(inSavedReport);
        mOrderID = inReport.getOrderID();
        mOrigOrderID = inReport.getOriginalOrderID();
        Instrument instrument = inReport.getInstrument();
        if (instrument != null) {
            mSecurityType = instrument.getSecurityType();
            mSymbol = instrument.getSymbol();
            InstrumentSummaryFields summaryFields = InstrumentSummaryFields.SELECTOR.forInstrument(instrument);
            mOptionType = summaryFields.getOptionType(instrument);
            mStrikePrice = summaryFields.getStrikePrice(instrument);
            mExpiry = summaryFields.getExpiry(instrument);
        }
        mAccount = inReport.getAccount();
        mSide = inReport.getSide();
        mCumQuantity = inReport.getCumulativeQuantity();
        mAvgPrice = inReport.getAveragePrice();
        mLastQuantity = inReport.getLastQuantity();
        mLastPrice = inReport.getLastPrice();
        mOrderStatus = inReport.getOrderStatus();
        mSendingTime = inReport.getSendingTime();
        mViewer = inSavedReport.getViewer();
    }

    /**
     * Saves this instance within an existing transaction.
     *
     * @param inManager the entity manager instance
     * @param inContext the persistence context
     *
     * @throws PersistenceException if there were errors.
     */
    void localSave(EntityManager inManager,
                   PersistContext inContext)
            throws PersistenceException {
        super.saveLocal(inManager, inContext);
    }

    @Override
    protected void preSaveLocal(EntityManager em, PersistContext context)
            throws PersistenceException {
        super.preSaveLocal(em, context);
        //Set the root ID on the object.
        if(getOrigOrderID() == null) {
            //This is the first order in this chain
            setRootID(getOrderID());
        } else {
            //fetch the rootID from the original order
            Query query = em.createNamedQuery("rootIDForOrderID");  //$NON-NLS-1$
            query.setParameter("orderID", getOrigOrderID());  //$NON-NLS-1$
            List<?> list = query.getResultList();
            if (!list.isEmpty()) {
                setRootID((OrderID) list.get(0));
            } else {
                Messages.LOG_ROOT_ID_NOT_FOUND.warn(this, getOrderID(),
                        getOrigOrderID());
                setRootID(getOrigOrderID());
            }
        }
    }

    @OneToOne(optional = false)
    PersistentReport getReport() {
        return mReport;
    }
    
    private void setReport(PersistentReport inReport) {
        mReport = inReport;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="value",
                    column = @Column(name = "rootID", nullable = false))})
    @Column(nullable = false)
    OrderID getRootID() {
        return mRootID;
    }

    private void setRootID(OrderID inRootID) {
        mRootID = inRootID;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="value",
                    column = @Column(name = "orderID", nullable = false))})
    OrderID getOrderID() {
        return mOrderID;
    }

    private void setOrderID(OrderID inOrderID) {
        mOrderID = inOrderID;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="value",
                    column = @Column(name = "origOrderID"))})
    OrderID getOrigOrderID() {
        return mOrigOrderID;
    }

    private void setOrigOrderID(OrderID inOrigOrderID) {
        mOrigOrderID = inOrigOrderID;
    }

    SecurityType getSecurityType() {
        return mSecurityType;
    }

    private void setSecurityType(SecurityType inSecurityType) {
        mSecurityType = inSecurityType;
    }

    @Column(nullable = false)
    String getSymbol() {
        return mSymbol;
    }

    private void setSymbol(String inSymbol) {
        mSymbol = inSymbol;
    }

    String getExpiry() {
        return mExpiry;
    }

    private void setExpiry(String inExpiry) {
        mExpiry = inExpiry;
    }

    @Column(precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE, nullable = true)
    BigDecimal getStrikePrice() {
        return mStrikePrice;
    }

    private void setStrikePrice(BigDecimal inStrikePrice) {
        mStrikePrice = inStrikePrice;
    }

    OptionType getOptionType() {
        return mOptionType;
    }

    private void setOptionType(OptionType inOptionType) {
        mOptionType = inOptionType;
    }

    String getAccount() {
        return mAccount;
    }

    private void setAccount(String inAccount) {
        mAccount = inAccount;
    }

    @Column(nullable = false)
    Side getSide() {
        return mSide;
    }

    private void setSide(Side inSide) {
        mSide = inSide;
    }

    @Column(precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE, nullable = false)
    BigDecimal getCumQuantity() {
        return mCumQuantity;
    }

    private void setCumQuantity(BigDecimal inCumQuantity) {
        mCumQuantity = inCumQuantity;
    }

    @Column(precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE, nullable = false)
    BigDecimal getAvgPrice() {
        return mAvgPrice;
    }

    private void setAvgPrice(BigDecimal inAvgPrice) {
        mAvgPrice = inAvgPrice;
    }

    @Column(precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE)
    BigDecimal getLastQuantity() {
        return mLastQuantity;
    }

    private void setLastQuantity(BigDecimal inLastQuantity) {
        mLastQuantity = inLastQuantity;
    }

    @Column(precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE)
    BigDecimal getLastPrice() {
        return mLastPrice;
    }

    private void setLastPrice(BigDecimal inLastPrice) {
        mLastPrice = inLastPrice;
    }

    @Column(nullable = false)
    OrderStatus getOrderStatus() {
        return mOrderStatus;
    }

    private void setOrderStatus(OrderStatus inOrderStatus) {
        mOrderStatus = inOrderStatus;
    }

    @Column(nullable = false)
    Date getSendingTime() {
        return mSendingTime;
    }

    private void setSendingTime(Date inSendingTime) {
        mSendingTime = inSendingTime;
    }

    @ManyToOne
    public SimpleUser getViewer() {
        return mViewer;
    }

    private void setViewer(SimpleUser inViewer) {
        mViewer = inViewer;
    }

    @Transient
    UserID getViewerID() {
        if (getViewer()==null) {
            return null;
        }
        return getViewer().getUserID();
    }

    /**
     * Defined to get JPA to work.
     */
    ExecutionReportSummary() {
    }

    private OrderID mRootID;
    private OrderID mOrderID;
    private OrderID mOrigOrderID;
    private SecurityType mSecurityType;
    private String mSymbol;
    private String mExpiry;
    private BigDecimal mStrikePrice;
    private OptionType mOptionType;
    private String mAccount;
    private Side mSide;
    private BigDecimal mCumQuantity;
    private BigDecimal mAvgPrice;
    private BigDecimal mLastQuantity;
    private BigDecimal mLastPrice;
    private OrderStatus mOrderStatus;
    private Date mSendingTime;
    private SimpleUser mViewer; 
    private PersistentReport mReport;
    /**
     * The attribute viewer used in JPQL queries
     */
    static final String ATTRIBUTE_VIEWER = "viewer";  //$NON-NLS-1$
    /**
     * The entity name as is used in various JPQL Queries
     */
    static final String ENTITY_NAME = ExecutionReportSummary.class.getSimpleName();
    /**
     * The scale used for storing all decimal values.
     */
    static final int DECIMAL_SCALE = 5;
    /**
     * The precision used for storing all decimal values.
     */
    static final int DECIMAL_PRECISION = 15;
    private static final long serialVersionUID = 1L;
}
