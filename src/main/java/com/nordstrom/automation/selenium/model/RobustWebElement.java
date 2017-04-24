package com.nordstrom.automation.selenium.model;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.automation.selenium.utility.UncheckedThrow;

public class RobustWebElement implements WebElement, WrapsElement, WrapsContext {
	
	/** wraps 1st matched reference */
	public static final int CARDINAL = -1;
	/** wraps an optional reference */
	public static final int OPTIONAL = -2;
	
	private static String LOCATE_BY_CSS = JsUtility.getScriptResource("locateByCss.js");
	private static String LOCATE_BY_XPATH = JsUtility.getScriptResource("locateByXpath.js");
	
	private enum Strategy { LOCATOR, JS_XPATH, JS_CSS }
	
	private WebDriver driver;
	private WebElement wrapped;
	private WrapsContext context;
	private By locator;
	private int index;
	
	private String selector;
	private Strategy strategy = Strategy.LOCATOR;
	
	/**
	 * Basic robust web element constructor
	 * 
	 * @param context element search context
	 * @param locator element locator
	 */
	public RobustWebElement(WrapsContext context, By locator) {
		this(null, context, locator, CARDINAL);
	}
	
	/**
	 * Constructor for wrapping an existing element reference 
	 * 
	 * @param element element reference to be wrapped
	 * @param context element search context
	 * @param locator element locator
	 */
	public RobustWebElement(WebElement element, WrapsContext context, By locator) {
		this(element, context, locator, CARDINAL);
	}
	
	/**
	 * Main robust web element constructor
	 * 
	 * @param element element reference to be wrapped (may be 'null')
	 * @param context element search context
	 * @param locator element locator
	 * @param index element index
	 */
	public RobustWebElement(WebElement element, WrapsContext context, By locator, int index) {
		
		// if specified element is already robust
		if (element instanceof RobustWebElement) {
			RobustWebElement robust = (RobustWebElement) element;
			element = robust.wrapped;
			context = robust.context;
			locator = robust.locator;
			index = robust.index;
		}
		
		this.wrapped = element;
		this.context = context;
		this.locator = locator;
		this.index = index;
		
		if (context == null) throw new IllegalArgumentException("Context cannot be null");
		if (locator == null) throw new IllegalArgumentException("Locator cannot be null");
		if (index < OPTIONAL) throw new IndexOutOfBoundsException("Specified index is invalid");
		
		driver = WebDriverUtils.getDriver(context.getWrappedContext());
		boolean findsByCss = (driver instanceof FindsByCssSelector);
		boolean findsByXPath = (driver instanceof FindsByXPath);
		
		if (index > 0) {
			if (findsByXPath && ( ! (locator instanceof By.ByCssSelector))) {
				selector = ByType.xpathLocatorFor(locator) + "[" + (index + 1) + "]";
				strategy = Strategy.JS_XPATH;
				
				this.locator = By.xpath(this.selector);
				index = CARDINAL;
			} else if (findsByCss) {
				selector = ByType.cssLocatorFor(locator);
				if (selector != null) {
					strategy = Strategy.JS_CSS;
				}
			}
		}
		
		if (element == null) {
			if (index == OPTIONAL) {
				acquireReference(this);
			} else {
				refreshReference(null);
			}
		}
	}
	
	@Override
	public <X> X getScreenshotAs(final OutputType<X> arg0) throws WebDriverException {
		try {
			return getWrappedElement().getScreenshotAs(arg0);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getScreenshotAs(arg0);
		}
	}

	@Override
	public void clear() {
		try {
			getWrappedElement().clear();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).clear();
		}
	}

	@Override
	public void click() {
		try {
			getWrappedElement().click();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).click();
		}
	}

	@Override
	public WebElement findElement(final By by) {
		return getElement(this, by);
	}

	@Override
	public List<WebElement> findElements(final By by) {
		return getElements(this, by);
	}

	@Override
	public String getAttribute(String name) {
		try {
			return getWrappedElement().getAttribute(name);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getAttribute(name);
		}
	}

	@Override
	public String getCssValue(String propertyName) {
		try {
			return getWrappedElement().getCssValue(propertyName);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getCssValue(propertyName);
		}
	}

	@Override
	public Point getLocation() {
		try {
			return getWrappedElement().getLocation();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getLocation();
		}
	}

	@Override
	public Rectangle getRect() {
		try {
			return getWrappedElement().getRect();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getRect();
		}
	}

	@Override
	public Dimension getSize() {
		try {
			return getWrappedElement().getSize();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getSize();
		}
	}

	@Override
	public String getTagName() {
		try {
			return getWrappedElement().getTagName();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getTagName();
		}
	}

	@Override
	public String getText() {
		try {
			return getWrappedElement().getText();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getText();
		}
	}

	@Override
	public boolean isDisplayed() {
		try {
			return getWrappedElement().isDisplayed();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isDisplayed();
		}
	}

	@Override
	public boolean isEnabled() {
		try {
			return getWrappedElement().isEnabled();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isEnabled();
		}
	}

	@Override
	public boolean isSelected() {
		try {
			return getWrappedElement().isSelected();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isSelected();
		}
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		try {
			getWrappedElement().sendKeys(keysToSend);
		} catch (StaleElementReferenceException e) {
			refreshReference(e).sendKeys(keysToSend);
		}
	}

	@Override
	public void submit() {
		try {
			getWrappedElement().submit();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).submit();
		}
	}

	@Override
	public WebElement getWrappedElement() {
		if (wrapped == null) {
			refreshReference(null);
		}
		return wrapped;
	}
	
	/**
	 * Determine if this robust element wraps a valid reference.
	 * 
	 * @return 'true' if reference was acquired; otherwise 'false'
	 */
	public boolean hasReference() {
		if ((index == OPTIONAL) && (wrapped == null)) {
			acquireReference(this);
			return (null != wrapped);
		} else {
			return true;
		}
	}
	
	/**
	 * Get the search context for this element.
	 * 
	 * @return element search context
	 */
	public WrapsContext getContext() {
		return context;
	}
	
	/**
	 * Get the locator for this element.
	 * 
	 * @return element locator
	 */
	public By getLocator() {
		return locator;
	}
	
	/**
	 * Get the element index.
	 * <p>
	 * <b>NOTE</b>: {@link #CARDINAL} = 1st matched reference; {@link #OPTIONAL} = an optional reference
	 * 
	 * @return element index (see NOTE)
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Refresh the wrapped element reference.
	 * 
	 * @param e {@link StaleElementReferenceException} that necessitates reference refresh
	 * @return this robust web element with refreshed reference
	 */
	private WebElement refreshReference(StaleElementReferenceException e) {
		try {
			long impliedTimeout = SeleniumConfig.getConfig().getLong(SeleniumSettings.IMPLIED_TIMEOUT.key());
			new SearchContextWait((SearchContext) context, impliedTimeout).until(referenceIsRefreshed(this));
			return this;
		} catch (Throwable t) {
			if (e != null) UncheckedThrow.throwUnchecked(e);
			if (t instanceof TimeoutException) UncheckedThrow.throwUnchecked(t.getCause());
			throw UncheckedThrow.throwUnchecked(t);
		}
	}
	
	/**
	 * Returns a 'wait' proxy that refreshes the wrapped reference of the specified robust element.
	 * 
	 * @param element robust web element object
	 * @return wrapped element reference (refreshed)
	 */
	private static Coordinator<WebElement> referenceIsRefreshed(final RobustWebElement element) {
		return new Coordinator<WebElement>() {

			@Override
			public WebElement apply(SearchContext context) {
				try {
					return acquireReference(element);
				} catch (StaleElementReferenceException e) {
					((WrapsContext) context).refreshContext();
					return acquireReference(element);
				}
			}

			@Override
			public String toString() {
				return "element reference to be refreshed";
			}
		};
		
	}
	
	/**
	 * Acquire the element reference that's wrapped by the specified robust element.
	 * 
	 * @param element robust web element object
	 * @return wrapped element reference
	 */
	private static WebElement acquireReference(RobustWebElement element) {
		SearchContext context = element.context.getWrappedContext();
		
		switch (element.strategy) {
		case JS_CSS:
			element.wrapped = JsUtility.runAndReturn(
					element.driver, LOCATE_BY_CSS, WebElement.class, context, element.selector, element.index);
			break;
			
		case JS_XPATH:
			element.wrapped = JsUtility.runAndReturn(
					element.driver, LOCATE_BY_XPATH, WebElement.class, context, element.selector);
			break;
			
		case LOCATOR:
			Timeouts timeouts = element.driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			try {
				if (element.index > 0) {
					element.wrapped = context.findElements(element.locator).get(element.index);
				} else {
					element.wrapped = context.findElement(element.locator);
				}
			} catch (NoSuchElementException e) {
				if (element.index != OPTIONAL) throw e;
				element.wrapped = null;
			} finally {
				long impliedTimeout = SeleniumConfig.getConfig().getLong(SeleniumSettings.IMPLIED_TIMEOUT.key());
				timeouts.implicitlyWait(impliedTimeout, TimeUnit.SECONDS);
			}
			break;
		}
		return element;
	}
	
	@Override
	public SearchContext getWrappedContext() {
		return getWrappedElement();
	}

	@Override
	public SearchContext refreshContext() {
		return refreshReference(null);
	}

	@Override
	public WebDriver getWrappedDriver() {
		return WebDriverUtils.getDriver(getWrappedElement());
	}
	
	/**
	 * Get the list of elements that match the specified locator in the indicated context.
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @return list of robust elements in context that match the locator
	 */
	public static List<WebElement> getElements(WrapsContext context, By locator) {
		List<WebElement> elements;
		try {
			elements = context.getWrappedContext().findElements(locator);
		} catch (StaleElementReferenceException e) {
			elements = context.refreshContext().findElements(locator);
		}
		for (int index = 0; index < elements.size(); index++) {
			elements.set(index, new RobustWebElement(elements.get(index), context, locator, index));
		}
		return elements;
	}
	
	/**
	 * Get the first element that matches the specified locator in the indicated context.
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @return robust element in context that matches the locator
	 */
	public static RobustWebElement getElement(WrapsContext context, By locator) {
		return getElement(context, locator, CARDINAL);
	}
	
	/**
	 * Get the item at the specified index in the list of elements matching the specified 
	 * locator in the indicated context.
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @param index element index
	 * @return indexed robust element in context that matches the locator
	 */
	public static RobustWebElement getElement(WrapsContext context, By locator, int index) {
		return new RobustWebElement(null, context, locator, index);
	}
	
}