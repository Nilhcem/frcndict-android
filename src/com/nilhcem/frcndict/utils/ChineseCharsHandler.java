package com.nilhcem.frcndict.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.nilhcem.frcndict.settings.SettingsActivity;

public final class ChineseCharsHandler {
	private static final String TAG = "ChineseCharsHandler";
	private static final ChineseCharsHandler INSTANCE = new ChineseCharsHandler();
	private static final String SAME_HANZI_REPLACEMENT = "-";
	private static final String FORMAT_HANZI_ST = "%s [%s]";
	private static final String FORMAT_HANZI_ST_HTML = "%s<font>&nbsp;</font><font>[</font>%s<font>]</font>";

	private String[] mColorsArray;

	private static final String[] PINYIN_NB2TONE_SRC = new String[] {
		"a1", "a2", "a3", "a4", "a5", "e1", "e2", "e3", "e4", "e5", "i1", "i2", "i3", "i4", "i5",
		"o1", "o2", "o3", "o4", "o5", "u1", "u2", "u3", "u4", "u5", "v", "u:", "ü1", "ü2", "ü3",
		"ü4", "ü5", "an1", "an2", "an3", "an4", "an5", "ang1", "ang2", "ang3", "ang4", "ang5", "en1",
		"en2", "en3", "en4", "en5", "eng1", "eng2", "eng3", "eng4", "eng5", "in1", "in2", "in3",
		"in4", "in5", "ing1", "ing2", "ing3", "ing4", "ing5", "ong1", "ong2", "ong3", "ong4", "ong5",
		"un1", "un2", "un3", "un4", "un5", "er1", "er2", "er3", "er4", "er5", "aō", "aó", "aǒ", "aò",
		"oū", "oú", "oǔ", "où", "aī", "aí", "aǐ", "aì", "eī", "eí", "eǐ", "eì"
	};
	private static final String[] PINYIN_NB2TONE_DST = new String[] {
		"ā", "á", "ǎ", "à", "a", "ē", "é", "ě", "è", "e", "ī", "í", "ǐ", "ì", "i",
		"ō", "ó", "ǒ", "ò", "o", "ū", "ú", "ǔ", "ù", "u", "ü", "ü", "ǖ", "ǘ", "ǚ",
		"ǜ", "ü", "ān", "án", "ǎn", "àn", "an", "āng", "áng", "ǎng", "àng", "ang", "ēn",
		"én", "ěn", "èn", "en", "ēng", "éng", "ěng", "èng", "eng", "īn", "ín", "ǐn",
		"ìn", "in", "īng", "íng", "ǐng", "ìng", "ing", "ōng", "óng", "ǒng", "òng", "ong",
		"ūn", "ún", "ǔn", "ùn", "un", "ēr", "ér", "ěr", "èr", "er", "āo", "áo", "ǎo", "ào",
		"ōu", "óu", "ǒu", "òu", "āi", "ái", "ǎi", "ài", "ēi", "éi", "ěi", "èi"
	};
	private static final String[] PINYIN_TONE2TB_SRC = new String[] {
		"āng", "áng", "ǎng", "àng", "ēng", "éng", "ěng", "èng", "īng", "íng", "ǐng", "ìng",
		"ōng", "óng", "ǒng", "òng", "ān", "án", "ǎn", "àn", "ēn", "én", "ěn", "èn",
		"īn", "ín", "ǐn", "ìn", "ūn", "ún", "ǔn", "ùn", "ēr", "ér", "ěr", "èr", "āo", "áo", "ǎo", "ào",
		"ōu", "óu", "ǒu", "òu", "āi", "ái", "ǎi", "ài", "ēi", "éi", "ěi", "èi", "ā", "á", "ǎ", "à",
		"ē", "é", "ě", "è", "ī", "í", "ǐ", "ì", "ō", "ó", "ǒ", "ò", "ū", "ú", "ǔ", "ù", "ǖ", "ǘ", "ǚ", "ǜ", "ü", "v"
	};
	private static final String[] PINYIN_TONE2TB_DST = new String[] {
		"ang1", "ang2", "ang3", "ang4", "eng1", "eng2", "eng3", "eng4", "ing1", "ing2", "ing3", "ing4",
		"ong1", "ong2", "ong3", "ong4", "an1", "an2", "an3", "an4", "en1", "en2", "en3", "en4",
		"in1", "in2", "in3", "in4", "un1", "un2", "un3", "un4", "er1", "er2", "er3", "er4", "aō", "aó", "aǒ", "aò",
		"oū", "oú", "oǔ", "où", "aī", "aí", "aǐ", "aì", "eī", "eí", "eǐ", "eì", "a1", "a2", "a3", "a4",
		"e1", "e2", "e3", "e4", "i1", "i2", "i3", "i4", "o1", "o2", "o3", "o4", "u1", "u2", "u3", "u4", "ü1", "ü2", "ü3", "ü4", "u:", "u:"
	};
	private static final String[] PINYIN_NB2RAW_SRC = new String[] {
		"a[1-5]", "e[1-5]", "i[1-5]", "o[1-5]", "u[1-5]", "u:[1-5]?", "an[1-5]", "ang[1-5]",
		"en[1-5]", "eng[1-5]", "in[1-5]", "ing[1-5]", "ong[1-5]", "un[1-5]", "er[1-5]"
	};
	private static final String[] PINYIN_NB2RAW_DST = new String[] {
		"a", "e", "i", "o", "u", "v", "an", "ang",
		"en", "eng", "in", "ing", "ong", "un", "er"
	};
	public static final String[] PINYIN_PIN2ZHU_SRC = new String[] {
		"chuang", "shuang", "zhuang", "chang", "cheng", "chong", "chuai", "chuan", "diang",
		"guang", "huang", "jiang", "jiong", "kuang", "liang", "niang", "qiang", "qiong",
		"shang", "sheng", "shuai", "shuan", "xiang", "xiong", "zhang", "zheng", "zhong",
		"zhuai", "zhuan", "bang", "beng", "bian", "biao", "bing", "cang", "ceng",
		"chai", "chan", "chao", "chen", "chou", "chua", "chui", "chun", "chuo", "cong", "cuan",
		"dang", "deng", "dian", "diao", "ding", "dong", "duan", "fang", "feng", "gang", "geng",
		"gong", "guai", "guan", "hang", "heng", "hong", "huai", "huan", "jian", "jiao", "jing", "juan",
		"kang", "keng", "kong", "kuai", "kuan", "lang", "leng", "lian", "liao", "ling", "long", "luan", "lu:an",
		"mang", "meng", "mian", "miao", "ming", "nang", "neng", "nian", "niao", "ning", "nong", "nuan",
		"pang", "peng", "pian", "piao", "ping", "qian", "qiao", "qing", "quan", "rang", "reng", "rong", "ruan",
		"sang", "seng", "shai", "shan", "shao", "shei", "shen", "shou", "shua", "shui", "shun", "shuo", "song", "suan",
		"tang", "teng", "tian", "tiao", "ting", "tong", "tuan", "wang", "weng", "xian", "xiao", "xing", "xuan",
		"yang", "ying", "yong", "yuan", "zang", "zeng", "zhai", "zhan", "zhao", "zhei", "zhen", "zhou", "zhua",
		"zhui", "zhun", "zhuo", "zong", "zuan", "ang", "bai", "ban", "bao", "bei", "ben", "bie", "bin",
		"cai", "can", "cao", "cen", "cha", "che", "chi", "chu", "cou", "cui", "cun", "cuo",
		"dai", "dan", "dao", "dei", "die", "diu", "dou", "dui", "dun", "duo", "eng", "fan", "fei", "fen", "fou",
		"gai", "gan", "gao", "gei", "gen", "gou", "gua", "gui", "gun", "guo", "hai", "han", "hao", "hei", "hen",
		"hou", "hua", "hui", "hun", "huo", "jia", "jie", "jin", "jiu", "jue", "jun",
		"kai", "kan", "kao", "ken", "kou", "kua", "kui", "kun", "kuo", "lai", "lan", "lao", "lei", "lia", "lie",
		"lin", "liu", "lou", "lu:e", "lun", "lu:n", "luo", "mai", "man", "mao", "mei", "men", "mie", "min", "miu", "mou",
		"nai", "nan", "nao", "nei", "nen", "nia", "nie", "nin", "niu", "nou", "nu:e", "nun", "nuo",
		"pai", "pan", "pao", "pei", "pen", "pie", "pin", "pou", "qia", "qie", "qin", "qiu", "que", "qun",
		"ran", "rao", "ren", "rou", "rui", "run", "ruo", "sai", "san", "sao", "sen",
		"sha", "she", "shi", "shu", "sou", "sui", "sun", "suo", "tai", "tan", "tao", "tie", "tou", "tui", "tun", "tuo",
		"wai", "wan", "wei", "wen", "xia", "xie", "xin", "xiu", "xue", "xun", "yan", "yao", "yin", "you", "yue", "yun",
		"zai", "zan", "zao", "zei", "zen", "zha", "zhe", "zhi", "zhu", "zou", "zui", "zun", "zuo",
		"ai", "an", "ao", "ba", "bi", "bo", "bu", "ca", "ce", "ci", "cu", "da", "de", "di", "du",
		"ei", "en", "er", "fa", "fo", "fu", "ga", "ge", "gu", "ha", "he", "hu", "ji", "ju",
		"ka", "ke", "ku", "la", "le", "li", "lo", "lu", "lu:", "ma", "me", "mi", "mo", "mu",
		"na", "ne", "ni", "nu", "nu:", "ou", "pa", "pi", "po", "pu", "qi", "qu", "re", "ri", "ru",
		"sa", "se", "si", "su", "ta", "te", "ti", "tu", "wa", "wo", "wu", "xi", "xu", "ya", "ye", "yi", "yu",
		"za", "ze", "zi", "zu", "a", "e", "o", "1", "2", "3", "4", "5"
	};
	public static final String[] PINYIN_PIN2ZHU_DST = new String[] {
		"ㄔㄨㄤ", "ㄕㄨㄤ", "ㄓㄨㄤ", "ㄔㄤ", "ㄔㄥ", "ㄔㄨㄥ", "ㄔㄨㄞ", "ㄔㄨㄢ", "ㄉㄧㄤ",
		"ㄍㄨㄤ", "ㄏㄨㄤ", "ㄐㄧㄤ", "ㄐㄩㄥ", "ㄎㄨㄤ", "ㄌㄧㄤ", "ㄋㄧㄤ", "ㄑㄧㄤ", "ㄑㄩㄥ",
		"ㄕㄤ", "ㄕㄥ", "ㄕㄨㄞ", "ㄕㄨㄢ", "ㄒㄧㄤ", "ㄒㄩㄥ", "ㄓㄤ", "ㄓㄥ", "ㄓㄨㄥ",
		"ㄓㄨㄞ", "ㄓㄨㄢ", "ㄅㄤ", "ㄅㄥ", "ㄅㄧㄢ", "ㄅㄧㄠ", "ㄅㄧㄥ", "ㄘㄤ", "ㄘㄥ",
		"ㄔㄞ", "ㄔㄢ", "ㄔㄠ", "ㄔㄣ", "ㄔㄡ", "ㄔㄨㄚ", "ㄔㄨㄟ", "ㄔㄨㄣ", "ㄔㄨㄛ", "ㄘㄨㄥ", "ㄘㄨㄢ",
		"ㄉㄤ", "ㄉㄥ", "ㄉㄧㄢ", "ㄉㄧㄠ", "ㄉㄧㄥ", "ㄉㄨㄥ", "ㄉㄨㄢ", "ㄈㄤ", "ㄈㄥ", "ㄍㄤ", "ㄍㄥ",
		"ㄍㄨㄥ", "ㄍㄨㄞ", "ㄍㄨㄢ", "ㄏㄤ", "ㄏㄥ", "ㄏㄨㄥ", "ㄏㄨㄞ", "ㄏㄨㄢ", "ㄐㄧㄢ", "ㄐㄧㄠ", "ㄐㄧㄥ", "ㄐㄩㄢ",
		"ㄎㄤ", "ㄎㄥ", "ㄎㄨㄥ", "ㄎㄨㄞ", "ㄎㄨㄢ", "ㄌㄤ", "ㄌㄥ", "ㄌㄧㄢ", "ㄌㄧㄠ", "ㄌㄧㄥ", "ㄌㄨㄥ", "ㄌㄨㄢ", "ㄌㄩㄢ",
		"ㄇㄤ", "ㄇㄥ", "ㄇㄧㄢ", "ㄇㄧㄠ", "ㄇㄧㄥ", "ㄋㄤ", "ㄋㄥ", "ㄋㄧㄢ", "ㄋㄧㄠ", "ㄋㄧㄥ", "ㄋㄨㄥ", "ㄋㄨㄢ",
		"ㄆㄤ", "ㄆㄥ", "ㄆㄧㄢ", "ㄆㄧㄠ", "ㄆㄧㄥ", "ㄑㄧㄢ", "ㄑㄧㄠ", "ㄑㄧㄥ", "ㄑㄩㄢ", "ㄖㄤ", "ㄖㄥ", "ㄖㄨㄥ", "ㄖㄨㄢ",
		"ㄙㄤ", "ㄙㄥ", "ㄕㄞ", "ㄕㄢ", "ㄕㄠ", "ㄕㄟ", "ㄕㄣ", "ㄕㄡ", "ㄕㄨㄚ", "ㄕㄨㄟ", "ㄕㄨㄣ", "ㄕㄨㄛ", "ㄙㄨㄥ", "ㄙㄨㄢ",
		"ㄊㄤ", "ㄊㄥ", "ㄊㄧㄢ", "ㄊㄧㄠ", "ㄊㄧㄥ", "ㄊㄨㄥ", "ㄊㄨㄢ", "ㄨㄤ", "ㄨㄥ", "ㄒㄧㄢ", "ㄒㄧㄠ", "ㄒㄧㄥ", "ㄒㄩㄢ",
		"ㄧㄤ", "ㄧㄥ", "ㄩㄥ", "ㄩㄢ", "ㄗㄤ", "ㄗㄥ", "ㄓㄞ", "ㄓㄢ", "ㄓㄠ", "ㄓㄟ", "ㄓㄣ", "ㄓㄡ", "ㄓㄨㄚ",
		"ㄓㄨㄟ", "ㄓㄨㄣ", "ㄓㄨㄛ", "ㄗㄨㄥ", "ㄗㄨㄢ", "ㄤ", "ㄅㄞ", "ㄅㄢ", "ㄅㄠ", "ㄅㄟ", "ㄅㄣ", "ㄅㄧㄝ", "ㄅㄧㄣ",
		"ㄘㄞ", "ㄘㄢ", "ㄘㄠ", "ㄘㄣ", "ㄔㄚ", "ㄔㄜ", "ㄔ", "ㄔㄨ", "ㄘㄡ", "ㄘㄨㄟ", "ㄘㄨㄣ", "ㄘㄨㄛ",
		"ㄉㄞ", "ㄉㄢ", "ㄉㄠ", "ㄉㄟ", "ㄉㄧㄝ", "ㄉㄧㄡ", "ㄉㄡ", "ㄉㄨㄟ", "ㄉㄨㄣ", "ㄉㄨㄛ", "ㄥ", "ㄈㄢ", "ㄈㄟ", "ㄈㄣ", "ㄈㄡ",
		"ㄍㄞ", "ㄍㄢ", "ㄍㄠ", "ㄍㄟ", "ㄍㄣ", "ㄍㄡ", "ㄍㄨㄚ", "ㄍㄨㄟ", "ㄍㄨㄣ", "ㄍㄨㄛ", "ㄏㄞ", "ㄏㄢ", "ㄏㄠ", "ㄏㄟ", "ㄏㄣ",
		"ㄏㄡ", "ㄏㄨㄚ", "ㄏㄨㄟ", "ㄏㄨㄣ", "ㄏㄨㄛ", "ㄐㄧㄚ", "ㄐㄧㄝ", "ㄐㄧㄣ", "ㄐㄧㄡ", "ㄐㄩㄝ", "ㄐㄩㄣ",
		"ㄎㄞ", "ㄎㄢ", "ㄎㄠ", "ㄎㄣ", "ㄎㄡ", "ㄎㄨㄚ", "ㄎㄨㄟ", "ㄎㄨㄣ", "ㄎㄨㄛ", "ㄌㄞ", "ㄌㄢ", "ㄌㄠ", "ㄌㄟ", "ㄌㄧㄚ", "ㄌㄧㄝ",
		"ㄌㄧㄣ", "ㄌㄧㄡ", "ㄌㄡ", "ㄌㄩㄝ", "ㄌㄨㄣ", "ㄌㄩㄣ", "ㄌㄨㄛ", "ㄇㄞ", "ㄇㄢ", "ㄇㄠ", "ㄇㄟ", "ㄇㄣ", "ㄇㄧㄝ", "ㄇㄧㄣ", "ㄇㄧㄡ", "ㄇㄡ",
		"ㄋㄞ", "ㄋㄢ", "ㄋㄠ", "ㄋㄟ", "ㄋㄣ", "ㄋㄧㄚ", "ㄋㄧㄝ", "ㄋㄧㄣ", "ㄋㄧㄡ", "ㄋㄡ", "ㄋㄩㄝ", "ㄋㄨㄣ", "ㄋㄨㄛ",
		"ㄆㄞ", "ㄆㄢ", "ㄆㄠ", "ㄆㄟ", "ㄆㄣ", "ㄆㄧㄝ", "ㄆㄧㄣ", "ㄆㄡ", "ㄑㄧㄚ", "ㄑㄧㄝ", "ㄑㄧㄣ", "ㄑㄧㄡ", "ㄑㄩㄝ", "ㄑㄩㄣ",
		"ㄖㄢ", "ㄖㄠ", "ㄖㄣ", "ㄖㄡ", "ㄖㄨㄟ", "ㄖㄨㄣ", "ㄖㄨㄛ", "ㄙㄞ", "ㄙㄢ", "ㄙㄠ", "ㄙㄣ",
		"ㄕㄚ", "ㄕㄜ", "ㄕ", "ㄕㄨ", "ㄙㄡ", "ㄙㄨㄟ", "ㄙㄨㄣ", "ㄙㄨㄛ", "ㄊㄞ", "ㄊㄢ", "ㄊㄠ", "ㄊㄧㄝ", "ㄊㄡ", "ㄊㄨㄟ", "ㄊㄨㄣ", "ㄊㄨㄛ",
		"ㄨㄞ", "ㄨㄢ", "ㄨㄟ", "ㄨㄣ", "ㄒㄧㄚ", "ㄒㄧㄝ", "ㄒㄧㄣ", "ㄒㄧㄡ", "ㄒㄩㄝ", "ㄒㄩㄣ", "ㄧㄢ", "ㄧㄠ", "ㄧㄣ", "ㄧㄡ", "ㄩㄝ", "ㄩㄣ",
		"ㄗㄞ", "ㄗㄢ", "ㄗㄠ", "ㄗㄟ", "ㄗㄣ", "ㄓㄚ", "ㄓㄜ", "ㄓ", "ㄓㄨ", "ㄗㄡ", "ㄗㄨㄟ", "ㄗㄨㄣ", "ㄗㄨㄛ",
		"ㄞ", "ㄢ", "ㄠ", "ㄅㄚ", "ㄅㄧ", "ㄅㄛ", "ㄅㄨ", "ㄘㄚ", "ㄘㄜ", "ㄘ", "ㄘㄨ", "ㄉㄚ", "ㄉㄜ", "ㄉㄧ", "ㄉㄨ",
		"ㄟ", "ㄣ", "ㄦ", "ㄈㄚ", "ㄈㄛ", "ㄈㄨ", "ㄍㄚ", "ㄍㄜ", "ㄍㄨ", "ㄏㄚ", "ㄏㄜ", "ㄏㄨ", "ㄐㄧ", "ㄐㄩ",
		"ㄎㄚ", "ㄎㄜ", "ㄎㄨ", "ㄌㄚ", "ㄌㄜ", "ㄌㄧ", "ㄌㄛ", "ㄌㄨ", "ㄌㄩ", "ㄇㄚ", "ㄇㄜ", "ㄇㄧ", "ㄇㄛ", "ㄇㄨ",
		"ㄋㄚ", "ㄋㄜ", "ㄋㄧ", "ㄋㄨ", "ㄋㄩ", "ㄡ", "ㄆㄚ", "ㄆㄧ", "ㄆㄛ", "ㄆㄨ", "ㄑㄧ", "ㄑㄩ", "ㄖㄜ", "ㄖ", "ㄖㄨ",
		"ㄙㄚ", "ㄙㄜ", "ㄙ", "ㄙㄨ", "ㄊㄚ", "ㄊㄜ", "ㄊㄧ", "ㄊㄨ", "ㄨㄚ", "ㄨㄛ", "ㄨ", "ㄒㄧ", "ㄒㄩ", "ㄧㄚ", "ㄧㄝ", "ㄧ", "ㄩ",
		"ㄗㄚ", "ㄗㄜ", "ㄗ", "ㄗㄨ", "ㄚ", "ㄜ", "ㄛ", "", "ˊ", "ˇ", "ˋ", "˙"
	};
	private ChineseCharsHandler() {
    }

	public static ChineseCharsHandler getInstance() {
		return INSTANCE;
    }

	public void setColorsArray(String[] colorsArray) {
		mColorsArray = colorsArray.clone();
	}

	// Transforms a pin1yin1 with tone numbers to a pīnyīn with tone marks
	private String pinyinNbToTones(String src) {
		return replaceStrWithArraysValues(src, ChineseCharsHandler.PINYIN_NB2TONE_SRC, ChineseCharsHandler.PINYIN_NB2TONE_DST);
	}

	// Transforms a pīnyīn with tone marks to a pin1yin1 with tone numbers
	public String pinyinTonesToNb(String src) {
		return replaceStrWithArraysValues(src, ChineseCharsHandler.PINYIN_TONE2TB_SRC, ChineseCharsHandler.PINYIN_TONE2TB_DST);
	}

	// Transforms a pin1yin1 with tone numbers to a pinyin without tone mark.
	private String pinyinNbToRaw(String src) {
		return replaceStrWithArraysValues(src, ChineseCharsHandler.PINYIN_NB2RAW_SRC, ChineseCharsHandler.PINYIN_NB2RAW_DST);
	}

	// Transforms a pin1yin1 with tone numbers to Zhuyin (Bopomofo)
	// http://zh.wikipedia.org/wiki/%E4%B8%AD%E6%96%87%E6%8B%BC%E9%9F%B3%E5%B0%8D%E7%85%A7%E8%A1%A8
	private String pinyinNbToZhuyin(String src) {
		return replaceStrWithArraysValues(src.toLowerCase(), ChineseCharsHandler.PINYIN_PIN2ZHU_SRC, ChineseCharsHandler.PINYIN_PIN2ZHU_DST);
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
		} else if (prefsPinyin.equals(SettingsActivity.VAL_PINYIN_ZHUYIN)) {
			return pinyinNbToZhuyin(pinyin);
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
		if (TextUtils.isEmpty(traditional)
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
			|| TextUtils.isEmpty(traditional)
			|| simplified.equalsIgnoreCase(traditional)) {
			return (prefColorHanzi) ? addColorToHanzi(left, pinyin) : left;
		} else { // Both are displayed
			right = replaceSameHanziByDash(left, right);

			// Color hanzi if needed
			if (prefColorHanzi) {
				left = addColorToHanzi(left, pinyin);
				right = addColorToHanzi(right, pinyin);
				return String.format(ChineseCharsHandler.FORMAT_HANZI_ST_HTML, left, right);
			}
			return String.format(ChineseCharsHandler.FORMAT_HANZI_ST, left, right);
		}
	}

	private String replaceSameHanziByDash(String base, String toReplace) {
		int length = base.length();
		if (length != toReplace.length()) {
			Log.w(ChineseCharsHandler.TAG, "[replaceSameHanziByDash] Size doesn't match: %s - %s", base, toReplace);
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
		if (TextUtils.isEmpty(pinyin)) {
			return hanzi;
		}

		String[] splitPinyin = pinyin.split("\\s");
		char[] splitHanzi = hanzi.toCharArray();

		int length = splitHanzi.length;
		if (splitPinyin.length == length) {
			StringBuilder sb = new StringBuilder();

			// loop for each hanzi
			for (int curHanzi = 0; curHanzi < length; curHanzi++) {
				int nbTones = mColorsArray.length;

				boolean foundColor = false;
				for (int colorNb = 1; colorNb <= nbTones; colorNb++) {
					if (splitPinyin[curHanzi].contains(Integer.toString(colorNb))
							/* TODO: and contains at least one character (to make sure it's not a number */
							) {
						sb.append("<font color=\"")
							.append(mColorsArray[colorNb])
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
			Log.w(ChineseCharsHandler.TAG, "[addColorToHanzi] Size doesn't match: %s - %s", hanzi, pinyin);
		}

		return hanzi;
	}

	private String replaceStrWithArraysValues(String str, String[] src, String[] dst) {
		int length = src.length;
		for (int i = 0; i < length; i++) {
			str = str.replaceAll(src[i], dst[i]);
		}
		return str;
	}
}
