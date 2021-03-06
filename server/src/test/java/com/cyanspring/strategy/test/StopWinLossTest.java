package com.cyanspring.strategy.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.sample.singleinstrument.StopWinLossStrategy;
import com.cyanspring.server.strategy.SingleInstrumentStrategyTest;

public class StopWinLossTest extends SingleInstrumentStrategyTest {

	@Override
	protected DataObject createData() {
		Instrument instr = new Instrument(getSymbol());
		instr.put(OrderField.STRATEGY.value(), "STOP_WIN_LOSS");
		instr.put(OrderField.POSITION.value(), 2000.0);
		instr.put(OrderField.POS_AVGPX.value(), 68.3);
		instr.put(StopWinLossStrategy.FIELD_MIN_WIN, 3.0);
		instr.put(StopWinLossStrategy.FIELD_HIGH_FALL, 1.0);
		instr.put(StopWinLossStrategy.FIELD_LOW_FALL, 3.0);
		instr.put(OrderField.STATE.value(), StrategyState.Paused);
		return instr;
	}
	@Override
	protected void setupOrderBook() {
		exchange.reset();
	}
	
	@Test
	public void testAllTimeLowHigh() {
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.1);
		timePass(strategy.getLpInterval());
		Instrument instr = strategy.getInstrument();		
		double ah = instr.get(double.class, OrderField.AHIGH.value());
		double al = instr.get(double.class, OrderField.ALOW.value());
		assertTrue(ah == 68.1);
		assertTrue(al == 68.1);
		
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.3, 68.3, 68.1);
		timePass(strategy.getLpInterval());
		ah = instr.get(double.class, OrderField.AHIGH.value());
		al = instr.get(double.class, OrderField.ALOW.value());
		assertTrue(ah == 68.3);
		assertTrue(al == 68.1);
		
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.0, 68.3, 68.0);
		timePass(strategy.getLpInterval());
		ah = instr.get(double.class, OrderField.AHIGH.value());
		al = instr.get(double.class, OrderField.ALOW.value());
		assertTrue(ah == 68.3);
		assertTrue(al == 68.0);
		
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.4, 68.3, 67.9);
		timePass(strategy.getLpInterval());
		ah = instr.get(double.class, OrderField.AHIGH.value());
		al = instr.get(double.class, OrderField.ALOW.value());
		assertTrue(ah == 68.4);
		assertTrue(al == 68.0);
	}

	@Test
	public void testHighFall() {
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.3, 68.3, 68.1);
		timePass(strategy.getLpInterval());
		Instrument instr = strategy.getInstrument();		
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 71.2, 71.2, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 71.3, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		// high fall
		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 69.8, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		ChildOrder child = childOrders.get(0);
		assertTrue(child.getPrice() == 69.7);
		assertTrue(child.getSymbol() == getSymbol());
		assertTrue(child.getSide() == OrderSide.Sell);
		assertTrue(child.getQuantity() == instr.getPosition());

		// missing bid
		setQuote(getSymbol(), 0, 0, 69.8, 20000, 69.6, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		child = childOrders.get(0);
		assertTrue(child.getPrice() == 69.6);
		assertTrue(child.getQuantity() == instr.getPosition());

		// bid volume is less than postion
		setQuote(getSymbol(), 69.7, 400, 69.8, 20000, 69.6, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		child = childOrders.get(0);
		assertTrue(PriceUtils.Equal(child.getPrice(), 69.65));
		assertTrue(child.getQuantity() == instr.getPosition());
	}
	
	@Test
	public void testFlatPosition() {
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 68.3, 68.3, 68.1);
		timePass(strategy.getLpInterval());
		setQuote(getSymbol(), 68.1, 20000, 68.3, 20000, 71.2, 71.2, 68.1);
		timePass(strategy.getLpInterval());
		
		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 71.3, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		
		// high fall
		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 69.8, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		Instrument instr = strategy.getInstrument();		
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		ChildOrder child = childOrders.get(0);
		assertTrue(child.getPrice() == 69.7);
		assertTrue(child.getSymbol() == getSymbol());
		assertTrue(child.getSide() == OrderSide.Sell);
		assertTrue(child.getQuantity() == instr.getPosition());
		
	}
	
	@Test
	public void testLowFall() {
		Instrument instr = strategy.getInstrument();		
		// low fall
		setQuote(getSymbol(), 65.3, 20000, 65.4, 20000, 65.3, 71.3, 65.3);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();		
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		ChildOrder child = childOrders.get(0);
		assertTrue(PriceUtils.Equal(child.getPrice(), 65.3));
		assertTrue(child.getQuantity() == instr.getPosition());
		
		this.enterExchangeBuyOrder(getSymbol(), 65.3, instr.getPosition());
		
		timePass(strategy.getLpInterval());
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		assertTrue(PriceUtils.isZero(instr.getPosition()));
	}	
	
	@Test
	public void testZeroPosition() {
		Instrument instr = strategy.getInstrument();		
		instr.put(OrderField.POSITION.value(), 0.0);
		// low fall
		setQuote(getSymbol(), 65.3, 20000, 65.4, 20000, 65.3, 71.3, 65.3);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		

		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 71.3, 71.3, 68.1);
		setQuote(getSymbol(), 69.7, 20000, 69.8, 20000, 69.8, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getInstrument();
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
	}	
}
