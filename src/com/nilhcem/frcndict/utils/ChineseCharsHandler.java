package com.nilhcem.frcndict.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.nilhcem.frcndict.settings.SettingsActivity;

public final class ChineseCharsHandler {
	private static final String TAG = "ChineseCharsHandler";
	private static final ChineseCharsHandler INSTANCE = new ChineseCharsHandler();
	private static final String SAME_HANZI_REPLACEMENT = "-";
	private static final String FORMAT_HANZI_ST = "%s [%s]";

	private String[] colorsArray;

	private ChineseCharsHandler() {
    }

	public static ChineseCharsHandler getInstance() {
		return INSTANCE;
    }

	public void setColorsArray(String[] colorsArray) {
		this.colorsArray = colorsArray.clone();
	}

	// Transform a pin1yin1 with tones number to a pīnyīn with tone marks
	// Ugly, rewrite better if possible
	public String pinyinNbToTones(String src) {
		String dest = src
				.replaceAll("a1", "ā")
				.replaceAll("a2", "á")
				.replaceAll("a3", "ǎ")
				.replaceAll("a4", "à")
				.replaceAll("a5", "a")
				.replaceAll("e1", "ē")
				.replaceAll("e2", "é")
				.replaceAll("e3", "ě")
				.replaceAll("e4", "è")
				.replaceAll("e5", "e")
				.replaceAll("i1", "ī")
				.replaceAll("i2", "í")
				.replaceAll("i3", "ǐ")
				.replaceAll("i4", "ì")
				.replaceAll("i5", "i")
				.replaceAll("o1", "ō")
				.replaceAll("o2", "ó")
				.replaceAll("o3", "ǒ")
				.replaceAll("o4", "ò")
				.replaceAll("o5", "o")
				.replaceAll("u1", "ū")
				.replaceAll("u2", "ú")
				.replaceAll("u3", "ǔ")
				.replaceAll("u4", "ù")
				.replaceAll("u5", "u")
				.replaceAll("v", "ü")
				.replaceAll("u:", "ü")
				.replaceAll("ü1", "ǖ")
				.replaceAll("ü2", "ǘ")
				.replaceAll("ü3", "ǚ")
				.replaceAll("ü4", "ǜ")
				.replaceAll("ü5", "ü")
				.replaceAll("an1", "ān")
				.replaceAll("an2", "án")
				.replaceAll("an3", "ǎn")
				.replaceAll("an4", "àn")
				.replaceAll("an5", "an")
				.replaceAll("ang1", "āng")
				.replaceAll("ang2", "áng")
				.replaceAll("ang3", "ǎng")
				.replaceAll("ang4", "àng")
				.replaceAll("ang5", "ang")
				.replaceAll("en1", "ēn")
				.replaceAll("en2", "én")
				.replaceAll("en3", "ěn")
				.replaceAll("en4", "èn")
				.replaceAll("en5", "en")
				.replaceAll("eng1", "ēng")
				.replaceAll("eng2", "éng")
				.replaceAll("eng3", "ěng")
				.replaceAll("eng4", "èng")
				.replaceAll("eng5", "eng")
				.replaceAll("in1", "īn")
				.replaceAll("in2", "ín")
				.replaceAll("in3", "ǐn")
				.replaceAll("in4", "ìn")
				.replaceAll("in5", "in")
				.replaceAll("ing1", "īng")
				.replaceAll("ing2", "íng")
				.replaceAll("ing3", "ǐng")
				.replaceAll("ing4", "ìng")
				.replaceAll("ing5", "ing")
				.replaceAll("ong1", "ōng")
				.replaceAll("ong2", "óng")
				.replaceAll("ong3", "ǒng")
				.replaceAll("ong4", "òng")
				.replaceAll("ong5", "ong")
				.replaceAll("un1", "ūn")
				.replaceAll("un2", "ún")
				.replaceAll("un3", "ǔn")
				.replaceAll("un4", "ùn")
				.replaceAll("un5", "un")
				.replaceAll("er1", "ēr")
				.replaceAll("er2", "ér")
				.replaceAll("er3", "ěr")
				.replaceAll("er4", "èr")
				.replaceAll("er5", "er")
				.replaceAll("aō", "āo")
				.replaceAll("aó", "áo")
				.replaceAll("aǒ", "ǎo")
				.replaceAll("aò", "ào")
				.replaceAll("oū", "ōu")
				.replaceAll("oú", "óu")
				.replaceAll("oǔ", "ǒu")
				.replaceAll("où", "òu")
				.replaceAll("aī", "āi")
				.replaceAll("aí", "ái")
				.replaceAll("aǐ", "ǎi")
				.replaceAll("aì", "ài")
				.replaceAll("eī", "ēi")
				.replaceAll("eí", "éi")
				.replaceAll("eǐ", "ěi")
				.replaceAll("eì", "èi");
		return dest;
	}

	public String pinyinTonesToNb(String src) {
		String dest = src
				.replaceAll("āng", "ang1")
				.replaceAll("áng", "ang2")
				.replaceAll("ǎng", "ang3")
				.replaceAll("àng", "ang4")
				.replaceAll("ēng", "eng1")
				.replaceAll("éng", "eng2")
				.replaceAll("ěng", "eng3")
				.replaceAll("èng", "eng4")
				.replaceAll("īng", "ing1")
				.replaceAll("íng", "ing2")
				.replaceAll("ǐng", "ing3")
				.replaceAll("ìng", "ing4")
				.replaceAll("ōng", "ong1")
				.replaceAll("óng", "ong2")
				.replaceAll("ǒng", "ong3")
				.replaceAll("òng", "ong4")
				.replaceAll("ān", "an1")
				.replaceAll("án", "an2")
				.replaceAll("ǎn", "an3")
				.replaceAll("àn", "an4")
				.replaceAll("ēn", "en1")
				.replaceAll("én", "en2")
				.replaceAll("ěn", "en3")
				.replaceAll("èn", "en4")
				.replaceAll("īn", "in1")
				.replaceAll("ín", "in2")
				.replaceAll("ǐn", "in3")
				.replaceAll("ìn", "in4")
				.replaceAll("ūn", "un1")
				.replaceAll("ún", "un2")
				.replaceAll("ǔn", "un3")
				.replaceAll("ùn", "un4")
				.replaceAll("ēr", "er1")
				.replaceAll("ér", "er2")
				.replaceAll("ěr", "er3")
				.replaceAll("èr", "er4")
				.replaceAll("āo", "aō")
				.replaceAll("áo", "aó")
				.replaceAll("ǎo", "aǒ")
				.replaceAll("ào", "aò")
				.replaceAll("ōu", "oū")
				.replaceAll("óu", "oú")
				.replaceAll("ǒu", "oǔ")
				.replaceAll("òu", "où")
				.replaceAll("āi", "aī")
				.replaceAll("ái", "aí")
				.replaceAll("ǎi", "aǐ")
				.replaceAll("ài", "aì")
				.replaceAll("ēi", "eī")
				.replaceAll("éi", "eí")
				.replaceAll("ěi", "eǐ")
				.replaceAll("èi", "eì")
				.replaceAll("ā", "a1")
				.replaceAll("á", "a2")
				.replaceAll("ǎ", "a3")
				.replaceAll("à", "a4")
				.replaceAll("ē", "e1")
				.replaceAll("é", "e2")
				.replaceAll("ě", "e3")
				.replaceAll("è", "e4")
				.replaceAll("ī", "i1")
				.replaceAll("í", "i2")
				.replaceAll("ǐ", "i3")
				.replaceAll("ì", "i4")
				.replaceAll("ō", "o1")
				.replaceAll("ó", "o2")
				.replaceAll("ǒ", "o3")
				.replaceAll("ò", "o4")
				.replaceAll("ū", "u1")
				.replaceAll("ú", "u2")
				.replaceAll("ǔ", "u3")
				.replaceAll("ù", "u4")
				.replaceAll("ǖ", "ü1")
				.replaceAll("ǘ", "ü2")
				.replaceAll("ǚ", "ü3")
				.replaceAll("ǜ", "ü4")
				.replaceAll("ü", "u:")
				.replaceAll("v", "u:");
		return dest;
	}

	// Transform a pin1yin1 with tones number to a pinyin without tone mark.
	// Ugly, rewrite better if possible. Can't do regex to replace [1-5] because some pinyin contains numbers which are not tones
	public String pinyinNbToRaw(String src) {
		String dest = src
				.replaceAll("a[1-5]", "a")
				.replaceAll("e[1-5]", "e")
				.replaceAll("i[1-5]", "i")
				.replaceAll("o[1-5]", "o")
				.replaceAll("u[1-5]", "u")
				.replaceAll("u:[1-5]?", "v")
				.replaceAll("an[1-5]", "an")
				.replaceAll("ang[1-5]", "ang")
				.replaceAll("en[1-5]", "en")
				.replaceAll("eng[1-5]", "eng")
				.replaceAll("in[1-5]", "in")
				.replaceAll("ing[1-5]", "ing")
				.replaceAll("ong[1-5]", "ong")
				.replaceAll("un[1-5]", "un")
				.replaceAll("er[1-5]", "er");
		return dest;
	}

	public boolean charIsChinese(char ch) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
		return (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
			|| Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
			|| Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block));
	}

	public String formatPinyin(String pinyin, SharedPreferences prefs) {
		String prefsPinyin = prefs.getString(SettingsActivity.KEY_PINYIN, SettingsActivity.VAL_PINYIN_TONES);

		if (prefsPinyin.equals(SettingsActivity.VAL_PINYIN_NONE)) {
			return pinyinNbToRaw(pinyin);
		} else if (prefsPinyin.equals(SettingsActivity.VAL_PINYIN_NUMBER)) {
			return pinyin;
		} else { // KEY_PINYIN_TONES
			return pinyinNbToTones(pinyin);
		}
	}

	public String formatHanzi(String simplified, String traditional, String pinyin, SharedPreferences prefs) {
		boolean prefColorHanzi = prefs.getBoolean(SettingsActivity.KEY_COLOR_HANZI, true);
		String prefHanzi = prefs.getString(SettingsActivity.KEY_CHINESE_CHARS, SettingsActivity.VAL_CHINESE_CHARS_SIMP);

		// Determine the position of the hanzi [SIMP-TRAD or TRAD-SIMP]
		String left;
		String right;
		if (traditional.length() == 0
			|| prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_SIMP)
			|| prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_BOTH_ST)) {
			left = simplified;
			right = traditional;
		} else {
			left = traditional;
			right = simplified;
		}

		// Only one displayed
		if (prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_SIMP)
			|| prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_TRAD)
			|| traditional.length() == 0
			|| simplified.equalsIgnoreCase(traditional)) {
			return (prefColorHanzi) ? addColorToHanzi(left, pinyin) : left;
		} else { // Both are displayed
			right = replaceSameHanziByDash(left, right);

			// Color hanzi if needed
			if (prefColorHanzi) {
				left = addColorToHanzi(left, pinyin);
				right = addColorToHanzi(right, pinyin);
			}

			// Return formatted String
			return String.format(ChineseCharsHandler.FORMAT_HANZI_ST, left, right);
		}
	}

	private String replaceSameHanziByDash(String base, String toReplace) {
		int length = base.length();
		if (length != toReplace.length()) {
			Log.w(ChineseCharsHandler.TAG, "Size doesn't match: " + base + " - " + toReplace);
			return toReplace;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char curChar = toReplace.charAt(i);

			if (base.charAt(i) == curChar) {
				sb.append(ChineseCharsHandler.SAME_HANZI_REPLACEMENT);
			} else {
				sb.append(curChar);
			}
		}
		return sb.toString();
	}

	// surround hanzi with html color tags depending on their tones
	private String addColorToHanzi(String hanzi, String pinyin) {
		// if pinyin is missing, return normal hanzi
		if (pinyin.length() == 0) {
			return hanzi;
		}

		String[] splitPinyin = pinyin.split("\\s");
		char[] splitHanzi = hanzi.toCharArray();

		int length = splitHanzi.length;
		if (splitPinyin.length == length) {
			StringBuilder sb = new StringBuilder();

			// loop for each hanzi
			for (int curHanzi = 0; curHanzi < length; curHanzi++) {
				int nbTones = colorsArray.length;

				boolean foundColor = false;
				for (int colorNb = 1; colorNb <= nbTones; colorNb++) {
					if (splitPinyin[curHanzi].contains(Integer.toString(colorNb))
							/* TODO: and contains at least one character (to make sure it's not a number */
							) {
						sb.append("<font color=\"")
							.append(colorsArray[colorNb])
							.append("\">")
							.append(splitHanzi[curHanzi])
							.append("</font>");
						foundColor = true;
						break;
					}
				}
				if (!foundColor) {
					sb.append(splitHanzi[curHanzi]);
				}
			}
			return sb.toString();
		} else {
			Log.w(ChineseCharsHandler.TAG, "Size doesn't match: " + hanzi + " - " + pinyin);
		}

		return hanzi;
	}
}
