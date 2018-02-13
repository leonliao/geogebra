package org.geogebra.main;

import java.io.File;
import java.util.TreeSet;

import org.geogebra.common.util.lang.Language;
import org.junit.Assert;
import org.junit.Test;

public class LocalizationTest {
	@Test
	public void gwtTranslationFilesShouldMatchLanguages() {
		File dir = new File(
				"../web/src/nonfree/resources/org/geogebra/web/pub/js/");
		TreeSet<String> available = new TreeSet<>();
		for (File f : dir.listFiles()) {
			available.add(f.getAbsolutePath());
		}
		for (Language lang : Language.values()) {
			File trans = new File(
					"../web/src/nonfree/resources/org/geogebra/web/pub/js/properties_keys_"
							+ lang.getLocaleGWT() + ".js");
			Assert.assertTrue(trans.getAbsolutePath(),
					available.remove(trans.getAbsolutePath()));

		}
		StringBuilder sb = new StringBuilder();
		for (String fn : available) {
			sb.append(fn + "\n");
		}
		Assert.assertEquals("", sb.toString());
	}

	@Test
	public void aliasesShouldBeRecognized() {
		checkAlias(Language.Hebrew, "he", "iw");
		checkAlias(Language.Norwegian_Bokmal, "no", "nb", "nb_NO", "no-NO",
				"no_NO");
		checkAlias(Language.Norwegian_Nynorsk, "nn", "no-NO-NY", "nn-NO");
		checkAlias(Language.Chinese_Simplified, "zh", "zh-Hans-CN", "zh-CN");
		checkAlias(Language.Chinese_Traditional, "zh_TW", "zh-Hant-TW",
				"zh-TW");
		checkAlias(Language.Indonesian, "id", "in");
		checkAlias(Language.Filipino, "fil", "tl");
		checkAlias(Language.Yiddish, "yi", "ji");
		checkAlias(Language.Mongolian, "mn", "mn-mn");
		checkAlias(Language.English_UK, "en-GB");
		checkAlias(Language.English_US, "en-US", "en", "whatever");
	}

	private void checkAlias(Language lang, String... aliases) {
		for (String alias : aliases) {
			Assert.assertEquals(alias + " should stand for " + lang,
					Language.getClosestGWTSupportedLanguage(alias),
					lang);
		}
	}
}