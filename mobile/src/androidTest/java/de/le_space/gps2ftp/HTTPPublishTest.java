package de.le_space.gps2ftp;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HTTPPublishTest {

	@Rule
	public ActivityTestRule<MobileMainActivity> mActivityTestRule = new ActivityTestRule<>(MobileMainActivity.class);

	@Before
	public void grantPermission() {
	//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.ACCESS_FINE_LOCATION");

			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.ACCESS_FINE_LOCATION");
	//	}
	}

	@Test
	public void sHTTPPublishTest() {

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ViewInteraction appCompatButton = onView(
				allOf(withId(android.R.id.button2), withText("Cancel")));
		appCompatButton.perform(click());


		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		ViewInteraction floatingActionButton = onView(
				allOf(withId(R.id.fab),
						withParent(allOf(withId(R.id.coordinatorLayout),
								withParent(withId(android.R.id.content)))),
						isDisplayed()));
		floatingActionButton.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		appCompatButton = onView(
				allOf(withId(android.R.id.button2), withText("Cancel")));
		appCompatButton.perform(click());

		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

		ViewInteraction appCompatTextView = onView(
				allOf(withId(R.id.title), withText("Settings"), isDisplayed()));
		appCompatTextView.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ViewInteraction radioButton = onView(
				allOf(withId(R.id.HTTP), withText("HTTP(S)"),
						withParent(withId(R.id.appwidget_protocol))));
		radioButton.perform(click());

		ViewInteraction editText = onView(
				withId(R.id.appwidget_host));
		editText.perform(replaceText("https://irinasenko.com"), closeSoftKeyboard());

		ViewInteraction editText2 = onView(
				withId(R.id.appwidget_username));
		editText2.perform(replaceText(""), closeSoftKeyboard());

		ViewInteraction editText3 = onView(
				withId(R.id.appwidget_password));
		editText3.perform(replaceText(""), closeSoftKeyboard());

		ViewInteraction editText4 = onView(
				withId(R.id.appwidget_remoteDirectory));
		editText3.perform(replaceText("/locations"), closeSoftKeyboard());

		ViewInteraction button = onView(
				allOf(withId(R.id.save_button), withText("Save")));
		button.perform(click());

		ViewInteraction floatingActionButton2 = onView(
				allOf(withId(R.id.fab),
						withParent(allOf(withId(R.id.coordinatorLayout),
								withParent(withId(android.R.id.content)))),
						isDisplayed()));
		floatingActionButton2.perform(click());

	}

}
