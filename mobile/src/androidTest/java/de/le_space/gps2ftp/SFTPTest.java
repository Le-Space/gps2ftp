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
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SFTPTest {

	String sftpUsername = "nico";
	String sftpPassword = "jekaterinburg";

	@Rule
	public ActivityTestRule<MobileMainActivity> mActivityTestRule = new ActivityTestRule<>(MobileMainActivity.class);
	@Before
	public void grantPermission() {


		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.ACCESS_FINE_LOCATION");

			getInstrumentation().getUiAutomation().executeShellCommand(
					"pm grant " + getTargetContext().getPackageName()
							+ " android.permission.ACCESS_FINE_LOCATION");
		//}
	}

		//https://github.com/apache/mina-sshd
		//https://stackoverflow.com/questions/11837948/using-apache-mina-as-a-mock-in-memory-sftp-server-for-unit-testing
	/*	ServerBuilder.builder().interactiveAuthenticator(new KeyboardInteractiveAuthenticator() {
			@Override
			public InteractiveChallenge generateChallenge(ServerSession session, String username, String lang, String subMethods) {
				return null;
			}

			@Override
			public boolean authenticate(ServerSession session, String username, List<String> responses) throws Exception {
				return false;
			}
		});
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(22);
		//sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hostkey.cer")));

		//List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
		//userAuthFactories.add(new UserAuthPasswordFactory());
		//sshd.setUserAuthFactories(userAuthFactories);
		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException {
				if(username.equals(sftpUsername) && password.equals(sftpPassword))
					return true;
				else
					return false;
			}
		});

		//sshd.setCommandFactory(new ScpCommandFactory());

		List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
		namedFactoryList.add(new SftpSubsystemFactory());
		sshd.setSubsystemFactories(namedFactoryList);
		try {
			sshd.start();
		} catch (Exception e) {
			e.printStackTrace();
		} */




	@Test
	public void sFTPTest() {

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
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		 appCompatButton = onView(
				allOf(withId(android.R.id.button2), withText("Cancel")));
		appCompatButton.perform(click());

		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ViewInteraction appCompatTextView = onView(
				allOf(withId(R.id.title), withText("Settings"), isDisplayed()));
		appCompatTextView.perform(click());

		ViewInteraction radioButton = onView(
				allOf(withId(R.id.SFTP), withText("SFTP"),
						withParent(withId(R.id.appwidget_protocol))));
		radioButton.perform(click());

		ViewInteraction editText = onView(
				withId(R.id.appwidget_host));
		editText.perform(replaceText("localhost"), closeSoftKeyboard());

		ViewInteraction editText2 = onView(
				withId(R.id.appwidget_username));
		editText2.perform(replaceText("le-space"), closeSoftKeyboard());

		ViewInteraction editText3 = onView(
				withId(R.id.appwidget_password));
		editText3.perform(replaceText("wrongpassword"), closeSoftKeyboard());

		ViewInteraction editText4 = onView(
				withId(R.id.appwidget_remoteDirectory));
		editText3.perform(scrollTo(), replaceText("/home/le-space/public_html"), closeSoftKeyboard());
		//String ts = getInstrumentation().getContext().getString(R.string.test_connection);
		String ts = getTargetContext().getString(R.string.test_connection);
		/*Context testContext = getInstrumentation().getContext();
		Resources testRes = testContext.getResources();
		String ts = testRes.getString(R.string.test_connection);*/

		/*InputStream ts = testRes.openRawResource(R.raw.your_res);

		assertNotNull(testRes); */

		ViewInteraction button = onView(
				allOf(withId(R.id.connectionTest_button), withText(ts)));
		button.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
/*
		ViewInteraction floatingActionButton2 = onView(
				allOf(withId(R.id.fab),
						withParent(allOf(withId(R.id.coordinatorLayout),
								withParent(withId(android.R.id.content)))),
						isDisplayed()));
		floatingActionButton2.perform(click());

		// Added a sleep statement to match the app's execution delay.
		// The recommended way to handle such scenarios is to use Espresso idling resources:
		// https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ViewInteraction appCompatButton2 = onView(
				allOf(withId(android.R.id.button1), withText("OK")));
		appCompatButton2.perform( click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		//we are automatically in the settings now because the password is wrong
		ViewInteraction editText10 = onView(
				withId(R.id.appwidget_password));
		editText10.perform(replaceText("test"), closeSoftKeyboard());

		ViewInteraction button3 = onView(
				allOf(withId(R.id.save_button), withText("Save")));
		button3.perform( click());

		//publish to sftp again!
		ViewInteraction floatingActionButton3 = onView(
				allOf(withId(R.id.fab),
						withParent(allOf(withId(R.id.coordinatorLayout),
								withParent(withId(android.R.id.content)))),
						isDisplayed()));
		floatingActionButton3.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//		ViewInteraction appCompatButton5 = onView(
//				allOf(withId(android.R.id.button1), withText("OK")));
//		appCompatButton5.perform(click());

	}

}
