package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.junit.ArtifactParams;
import com.nordstrom.automation.junit.HookInstallingRunner;
import com.nordstrom.automation.junit.MethodWatchers;
import com.nordstrom.automation.junit.RuleChainWalker;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for JUnit.
 */
@RunWith(HookInstallingRunner.class)
@MethodWatchers({DriverWatcher.class})
public abstract class JUnitBase implements TestBase, ArtifactParams {
    
    /** This class rule performs end-of-class clean-up of drivers and local Grid. */
    @ClassRule
    public static final ExternalResource RESOURCE = DriverWatcher.getClassWatcher();
    
    /** This method rule manages driver lifetimes and opens initial pages. */
    
    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(new ScreenshotCapture(this))
            .around(new PageSourceCapture(this))
            .around(DriverWatcher.getTestWatcher(this));
    
    private Optional<WebDriver> optDriver = Optional.empty();
    private Optional<Page> optInitialPage = Optional.empty();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WebDriver> nabDriver() {
        return optDriver;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WebDriver> setDriver(final WebDriver driver) {
        optDriver = TestBase.optionalOf(driver);
        return optDriver;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> nabInitialPage() {
        return optInitialPage;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> setInitialPage(final Page pageObj) {
        optInitialPage = TestBase.optionalOf(pageObj);
        return optInitialPage;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTest(final Method method) {
        return null != method.getAnnotation(Test.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeMethod(final Method method) {
        return null != method.getAnnotation(Before.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterMethod(final Method method) {
        return null != method.getAnnotation(After.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeClass(final Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterClass(final Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescription() {
        return getLinkedRule(ScreenshotCapture.class).getDescription();
    }
    
    /**
     * Get the test rule of the specified type that's attached to the rule chain.
     * 
     * @param <T> test rule type
     * @param testRuleType test rule type
     * @return {@link ScreenshotCapture} test rule
     */
    public <T extends TestRule> T getLinkedRule(final Class<T> testRuleType) {
        Optional<T> optional = RuleChainWalker.getAttachedRule(ruleChain, testRuleType);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new IllegalStateException(testRuleType.getSimpleName() + " test rule wasn't found on the rule chain");
    }
}
