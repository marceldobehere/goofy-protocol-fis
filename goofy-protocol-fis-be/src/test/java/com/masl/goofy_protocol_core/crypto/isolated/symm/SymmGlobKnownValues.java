package com.masl.goofy_protocol_core.crypto.isolated.symm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.entry;

class SymmGlobKnownValues {
	public static final String SAMPLE_SECRET = "AMAZING INCREDIBLE SECRET FOR THE SYMMETRIC ENCRYPTION";
	public static final String SAMPLE_STR = "This is a test string! This is a test string yes! This is a test string maybe! This is a test string! insanely long text! AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
	public static final byte[] SAMPLE_ARR = SAMPLE_STR.getBytes(StandardCharsets.UTF_8);
	public static final Map<SymmCryptoType, KnownValueSet> knownValues = Map.ofEntries(
			entry(SymmCryptoType.AES_GCM_128, new KnownValueSet(
					SAMPLE_SECRET,
					SAMPLE_STR,
					"AES_GCM_192.uTnZAi7PCbkY3YD5pex3YkgcsIfybSQzAqAB_aGb70BRbQWFH3iC-N2v_XFGBbtjLIp71YH8D-fQ7OICvqOLUOctN_NXhay3WGuVQEXYLB_qH108HLVO3jivebW1DAFYzBZYR9-97zAiuwR-DK_rmdD6AC71BGcYjrs2WbxD1de1qaRZseuK34tBXnnM-pfS2bSa-YNJ44AA_rXlGd_3IGSYDq-yXaA9O8ke0y4lGwntUaqMzU5qEVEQ918A1KKfrmm5Be1u9VbAdU0BEtR_qtcehIiZHpVee28PQmpRROSh7asKQZ1_Y6OXwB7Gd1dqclNgpClJhiqutbB3EUjJeaO-BVlcNS3YhXe-6HREgBI9pJRH5nM2hPnbHbvpFTiEVVw-BhUqmSyEx09Aejo2wY9rQEeCdHawz0Ze51AAtmyeQHuJREoQ-mrAaMPzea2A8tTHKCID26xMDGd0OIDJ6BrnKiVCb5EDNS9JJ46AKCuXi_AU7yc9y0aGCB4SyWINZb9jmCBw_7EG07NerKKK6K1LfYW9W7t6pqvsGbXb7Y6TH_0tybaFNGeEqMtXNk1pV8CqEdDmsH2Q35FvQRCSMbzV4n2fIGc=",
					SAMPLE_ARR,
					Base64.getDecoder().decode("AQGSgJgK0GDVYTaMPpeKYisSRzV1mKuiYUL3pP295HoosSrvIPrHiMdqV/vG/9moweVPonGjDv0h7/Yo6gTD1eTpU4sh/8Jk1lX/9wlDly9OfObt9jqVOik+2BCpTMO9jp62S5katyxJpCbtsGIuK/E+iAxCN7fgCXtML/MOy3zRKvk5zpnkSmUhUBgAEgZRblVib7ExU4UXsZmJIvNZ300UnUCGaSYAcKdCmXiQC0Xkjd7QeDzq07GObdi96d5ioUyPuldV66JfLyRM119AqoX8zfCHhjYNDkG8jKV5xduTX6qVlvlzgmESsKb0r1YMo9VeMuHgFNJ5ZP0eNUvh+Lly9GkpFD2LUpSs3bHBzMicQ7fQpYI7zZ9mByzhAycXPti7xwoCVI1bQGSni/j2CmWYb8rXfOQOrPs+YdlBL89fFT68afXwt9Dja1MZBMYJfem//3oIhtAGklSfUWzyF2Hl36m4rO+ekFMu8DwbWcHDzSpA7PS/WdG7PyYa6hbjmLRs0JNoBjIaiWQ+8ot6gjwrU4IULaeIqP0AkEaU1aLb7f31naNOQeXEn3+dYuIzizKdllkBa3LLtTKHKc+MuNSpvi33WD2wrw=="),
					"AES_GCM_192.oPNPw0vaiYIFwGmPBPtFEOObzinO4q4udLHXi9U00l2qDwGtIKVtt1qvqHR5FkiPhwSLdgmfrfYH4zUxn4otJFI3ERC-ZCZ7M8xOSge8Nw9TokPdkp606V_HX34o4d1IbQyzMpAz-gzfOnDYhrla2qpFBWMSvPZtHvkI3J_Bue-vhsQ956HcXnCTrDYsvj_w-g6oWS4ezzNFVlcSupr8CdBLQwM2JtUfU0hovLM5G6HDA0ZyangS6RIwEWJ8oLJ1AMy1pbapNkCHB5GWMc3hyRYTc3b3x34QeM7ei_Kx1gIQvngKxxqcePxWz8XS4cILu84erSJSQdCCI5vMJTwRBs10T2ITH7qO679ySCdXYLCg01vfpJKvUuu-8XAIEPp-a--S4KVWcV-3DAQoyQy2Kcs5tbqa18t3Mpg88JJ8Tvdx2GjREglm7nlo23_eA5XWrDCnMaXaqWJ4hez0QN2UCsaNT--dUxF1bx9KYqaNTkdXNImNnoCHMhFo19Lli0hAFqTh5M2N_UF0dG4nO80XtcKdge2-093Q5hMF_pib67lG-MZgbsn3y89_NR7wTa8_elP1yDm2XoVOXoqrOw6z445ytZPbK0g="
			)),
			entry(SymmCryptoType.AES_GCM_192, new KnownValueSet(
					SAMPLE_SECRET,
					SAMPLE_STR,
					"AES_GCM_192.kxgZ9kjoXx1bAfy2qEV6LLdnM44VgR30ax4SVDYWW4Ud_DiFCH4ZCeRgvHColfrI7U5B92pW2hkctwSkQXK9kqp445MhrcmhNpKlq90nCD5DkbnTcgRx2QwkNQpyazhxIQZNLoRXiKQa_eIs8ecMbpgJDEzbl5KT2mgJCS0uOnccqzG5enjEmpbj9jIXx9dXYXuy8PQ8XN4RWjTTiuH1awtq3x_0C2iqNyksZ75321W0X2-FP1BbaDywdbC2QamI3pnNntgC9kp3lEX7O_DYvrWlVg0p-J5GWlJHEYJEkQxV6gDnrj86K5lP7ZfOdGiTdBx89_a1pQZa6k023P2Wi-eNYiuAHPLPpGMChiW1pQUxOlt4-hbOwDUyBG_58ZimUNQSeAC3PuKjoQk_sbaJZKqDMu-ymfx4-jexwCbRDrNzcbPmwROT3IZxMMLdrEi-CwrfQJxhtQs8W1mmVFp-ZQXMeW0K6efmK9iQ2Px4MFWo86PkIsO_nMw8QdGAOdSLCR33BEExcqMybusjIX041Asn8Y9D11pv5geXiGN6P_qGCx6TwL9aG3KFCU__NLI33-Dzm2kFJIeehduOW2Yy-OSnnbvwtTg=",
					SAMPLE_ARR,
					Base64.getDecoder().decode("AQEyEeSz2Bm8tnchEa5VBHSltTcY9M20mxtI13ICVDEaHNQslV3BJKxkbJ5NKo+wb4yIplMoI7kyCAgSDplH1dL4Z3WmW0rXmlakcuFu1APAoKMh7AWYWXwMDMW5/HNsNaGDjFifaP2Ie95MYIAVKa7a5YflmvzFDagXWHqCLnT0xBpYDe6f4htJiwsnoXdPzZBswtfV2KcDQO6bKuF4Y2KbDKGUKMK8lxq4vHnkFVTB1cQk4vm0BXIaS8fi6wwlam0dWoJPVUaPPCpEBaeF0ToIzr3cRu3f4ktj25DphFlqAUqSRquaBBXVq66eSUa/uoeBpzvdgEmOsFzYJIRLjLxbYihBhgj3jRVVQ6/haEtEXPv5GcHn0QnYp6FeYzAYT83l+y0AxhZ9OR0Ok8O4OMNnup0bp2qYosQJ8vlwrll6XdcsfdO1yeGPiKgzr7mm+QqvUkgp+to5KKVCefxrnw8GUPEf7YytsF4kWxqL7a0KHONEXa3GcWMZI4fv5XC/RZQmRtCyqIgR9lqJZ77VNe0l2r5bsviT0vOCRQXzvn9Sh2nA043cl0py/uOK1vZz56l0maVCH4Uz8iRPSnMfZnBMYFQAqoYWQw=="),
					"AES_GCM_192.3vtBKNSrb8vOIGldirb0DvLjf7JkpJOe3Sof5GpjgWEG5BW7EGDkYRdkComwaIpzGSZcsRXfW4Hgh4K7w4fKgMhXnLec_W6dxERBwnu8lbYgnNQA1B0FObGgdLG3a6WAWm7PNAKPrieljxi2PpM4hCdYtHVMilpLT3GF2yeLIaMagU02Gv57qmqbt26rjH_nCq893pK0oKmhzFv2LBxOLo2OPot3qAMKa7ATjrFWilPW1mx0XqllTh4CGF4Tnd-3NWQhfxhGcF7wYG-Mm81GBCVmuat2Njd7Ijpb9ZCdCjn3GXGFzigIYytXeK_yf3nHMel3o3P6sffjFZrwLBJCuydvKF9KfT_0K4dPvuVM6WIOPvuiVNytIqqQrkxdEKv4OtHhgYA0B2XrkT8g3Gq6d1q8BVNDJgzqd5L_-tzCDAfxG7vn_x3XrI5vRMU6COkwAeDYKeAQPXImznvjhSfZ_q3K3QiFc9lmlVMN7zAL7ZE2F0BpCaTMe8yMEcZk_xxfGk88YUnKJnAaklvSq2-myEhkvweqhayzGVj2VkZNzpoVcWccd7XSU0l82LxbqgfoANyeiF1oYYz0LdMNklg0CmqfYWsIQ8U="
			)),
			entry(SymmCryptoType.AES_GCM_256, new KnownValueSet(
					SAMPLE_SECRET,
					SAMPLE_STR,
					"AES_GCM_192.ZmQyJK-1dPelOHW0eoeVn1qbFlwa98gXep7QIa67IBeU93-InVf8Amxvy7V1EQOdqcNMANRaHThSAXSng-Qw9qkGz_xcUdN1C1-wIqsKeH_jlVjhNUaXsy0iZJlsN7umgTkNF3qddtK4NzdEhvg3dPibMNuAI-1Pdhuv71W-OJgvtc2fT1hsKWUzdzfdR8w1MQB_mTK3HHPCYy24n_Uegblt_r7wbgaWDep1TjmkY59y3Gf2wks8gm-FQJddmDU1PiGomWklJPn_Ah89v-7cmMgP25NxXU9VpIu6wIvNHWZOSpHO_0ocXbZ-lSShgNFp6_vb35innXX0SHHHh2BlgODdeThgEFGqdZ6mjzoVUEjTiq1qrW8Df4fZlzMZjh3FFdrarpDnys8T2mnKVMg4H8dp2WBsHFCDSk0UmFv0f3T3RRFlRx8N5vyPXpdZitKM87VOoA7t_Rdl3_BSIn_IRRxoGHJVF4DiibTz9hUQ1NOh5cYmxyjgK-2CNg8F0jzXXBFJ9Syc7VEuPHBnYx43LTi8kpp4HHjMCFkFkk0HrU7zyfgt6SQ1r-evn_XVEdxAqEtCfqtNG2C6MTQg1vep5tTelIU3K1s=",
					SAMPLE_ARR,
					Base64.getDecoder().decode("AQHy0rGsrsg5X4rYwcRKFxDIqjUIITuk+hZQuaYk8ZRqp41f6EhlTJSWmhruyjDwzSozo2rAeLTiGzZKKmBUUmV6hnlGF71sdnBzkVOndPF0R4JPz/YiVGrS4tMQ6N3o4WW5OBNzy2lQE1D8Q42U1R4kmfZO8pTbg+tRKceL7XNNI+rGQpMn9aDgZVrGnyN3W82RLLsJ8qmXOIAgXdmobAm5d8OpCz9PdWiED50dON7wTbstCvxCeGDq39OTUjF7kcGbezkny6Rbi4MmLIf7ypa/AK0+z1Dq6FWJbK+YGpQzUbN9V1seWUcC5/72dhwsnNMt71r/8EokmcPUfyNp1m6y5xQ/HiY3HXKkx5uciuWctJyzR0zgX0OU1E3Tfqnu59Q6nq3XkOTuAQWBN7B3b7v+eO6ecZlVcbaTtmcoVPJeBxAuCfr/fv3JLYzqN/q5pKDs6fWLAAgAKCBxPIu0YiQ2913kuTlwEAXoRhQ+5cW214Z/0YB7wzKpElKskOEecpUgL/O+mbZZFbaZ8bCNRl/gfA9pF5YoUPJGiPf8qqG2L27ZMvHJ4EVRKmALRQ1HTh5FM8ojDJCt+qRK9wV7U/qnb5CeFUMWAQ=="),
					"AES_GCM_192.wMbvc0ZDNVNnqsIUIstj8zSa7PpbhbwJbeo4Zbj2Or2QecMmzTKoAflbfOYKZArDPsHLAqHi6Z-m5XnqLMyDNwS6y82dDAuFG8sBGCzdLwxOghZ6VpAaGrK1VdKak1yQI9HMQTybFeT9NKyiT82UJjnIsnqUJCE04lzl1YUvjWuXveHxL9UT3bspsC-SSVonthm-uQ20oFndRDSFVTcJRPViiuZOuF5LtTfiGBINGX57omYWjzV5rPgt5sH7AZloeLoagnce175P7uqcACq-Mcr-QrsHQAZw8ExE35lgYqD6Wv084KA6WacxV5l8CFk1N7sAvtHsN0wNrk4-3yc1j-EDknF_8-7tX83HPIE8QcZe_nzvp-FpZnRqCT4AECdCqQoXHQAu-DM0w1YK7p47WSr6jwuqpWLexLlvvWvQzdzhZQCKp_OXzuA6bkWT9CUO6x4CqXgFWBB-ZvpbcNEZKMYc3RK9VieDt-PeLRqt2qvxpD9gFvBAqWOZqyOmdzkdlMf2rKLzrtmq_W9d1TEBfVGqnGN3qYPsX8GChqKu7Zf9NjAPqmFVTy5XnzZncE5BbwXgJ-klN72_3MkfNtQ1dpRMiNb18_c="
			)),
			entry(SymmCryptoType.CHACHA_20, new KnownValueSet(
					SAMPLE_SECRET,
					SAMPLE_STR,
					"AES_GCM_192.AP-5zohruySCHKb6F_hr0nEHXZAgzVCibJF9jEg3yBbvqok6TKgUnVL__a3KJ6isLQhreKkYEBBZgvF5sSjEx_BT7wLb-xg0OZEBWGEGkXNELuwPzkV723eGdni7by7N-A3MRCHGbIMWQYj92Kl6Sphm7YjEwoRGTp-pqkEuGgQO_RKakY7XpVud39fB1jH9DZ3lvzrUauIClQ7J66o9eUhFn-DLc9Ye0HXRLAU-Ni7-vQ6P8NXTbHSoCjxA_3Wn5z1v7or10per8lYUaEkE80W4XjRdF1hPp4V1aR79-O5-tGJEdDW58RoT-pJ4Xek4qBa_9tF0CFkbBJdhOIk3uajGlt7p-Zcbz2Es4AmgabLO_r9Vf23dQEShBbT3x2ChpQu603bWCim_kmxhaDWSDPRwX-dDqLQ_WXU2DeeHVaA4owJx0WgAyuDjWr9Hdxr7T8gZfga8F2u2Fwk-upJQZNGgraepg1zUNn_3O-G0VMTyNRkkWlA53HQRAkP6FUbbRcZN81LPurVt-E4kDJD3RcZ2UQFMu91l9T8HWdV4OknuoJia4ZxTix-qhzov_AtYPqIkF62jDEDu_jGVxYlw-MI-WXiHNDM=",
					SAMPLE_ARR,
					Base64.getDecoder().decode("AQH6iqrHbOnP4lkEK13d5Ed+6mojbqCK0vYC5dEBfveI3TX/DtpBP60bNShybKqaHRm722943ChXa5Yd4KwB5A0yuT/dIiCDgh0dwC0/w9Ds4jVx79VybnmDUHNh7vinHNyP0cw9FZjfB+DOme54zYHmpp6yhMpPxSBjPVz/joEhH4x+O88tgFvqUVNS81USdTCP/6e+B5+H/FtAiHnb+XpujEUsm1rHg6aEtWx7bL/ChdbfrXocv15JxcFImmHKvy1OzOwMquVG3VAbfrTUlf1nH5lRxMdL0X6BDMgt4d5wKPC5yvgnSZea3d+PBAOVjAvMBrhB8I28EGgf4OGk2g3AUh3ndLXLLLwzy++MhSTwZxifrtREP012e2K/uPaYsZZ1oDiMt1gGLhqsIWKgnD/7vmet575/D7Mh2zRMEteDEOzqpKmaiZehvGRBNqv8abHGgD+yx0qvJWBRGOcIS6HZ/dS4M+InuIOuub1OjtGMztsetdX9BnykrEg7Tq+u48r1HTiKky7MGYdTlsJdmmo75LeSezaeW0H3nyNYu1XClGNefhFvORE4AAfonKq9H0djsnOsuFF//yVGX039MwSfhjpzpanRIw=="),
					"AES_GCM_192.J7aDVQtiTsg72lwMvykTjlGgweAs-P22y2dCxMwTKBD-JCEljqRXbo9yCIHwkxpefztfbDCYMMgzErOrCS8D81JnUjBDn8nIpx2fHPIlfX1S4wfA8EuQZ2b0-ddpbGKTea2Iv_clct_QP955wCjpUPq50oWcIDI7qxLbQbPqh97RVw9kDgzniXQjJKmC0NMQutVAHrZDDHDstgHg0_f9uGzn9iQKCbwUbgpsoYHxZ6myJVau0moVklc2tL-ofkIVOIacxmuGc5oligFg6FJxmO1MCof3rFb4aPe8fPDEVmiXYqGvE95Eo74qOPyQ6E88U7PpIklBhB5hIH0uJqbfFpE3yl01GKsAEB3JJWvwH8Ry8omi67woFVhKNoRMjYW27Upycx-lM9fFS-RPWpYBRCcN0KcB3UxmEpjqJrTg23pLmTrGqMuIRkrVG-81cD45j_sycVvJHv1qJ_yBx9chN2diJWoxiLKqRwQMtlsuavlEn-_rs_4gWoAs3aj4_DLZM07wD1K3cGpdyYG_mJIONOeASAtXoSFgrt1dfaxROm9VHJE5BmYD15uBoMk8vUCPaH8cbYrfV59BgWmLhkDqRVhr7Ikk9Ho="
			))
	);

	public record KnownValueSet(String secret, String strOg, String strEnc, byte[] rawDefOg, byte[] rawEnc, String defEnc) {
		@Override
		public String toString() {
			return "<KnownValueSet>";
		}
	}

	private final GlobSymmCrypto crypto = new GlobSymmCrypto();

	@BeforeAll
	static void init() {
		Security.addProvider(new BouncyCastleProvider());
		Security.addProvider(new BouncyCastlePQCProvider());
	}

	// Might make it generate a file and load it from there later but oh well
	@Test
	@Disabled("Just a cursed Helper Method to generate the map so I can copy paste it or regenerate it if needed")
	void _generateKnownValues() {
		for (var type : crypto.getTypes()) {
			// Encrypt String & Raw & Dev
			String strEnc = crypto.encryptStr(SAMPLE_STR, SAMPLE_SECRET);
			String rawEnc = Base64.getEncoder().encodeToString(crypto.encryptRaw(SAMPLE_ARR, SAMPLE_SECRET));
			String defEnc = crypto.encrypt(SAMPLE_ARR, SAMPLE_SECRET);

			boolean isLast = type.equals(SymmCryptoType.values()[SymmCryptoType.values().length - 1]);

			// Create String that looks like entry
			String entryStr =
					"entry(SymmCryptoType." + type + ", new KnownValueSet(\n" +
							"\tSAMPLE_SECRET,\n" +
							"\tSAMPLE_STR,\n" +
							"\t\"" + strEnc + "\",\n" +
							"\tSAMPLE_ARR,\n" +
							"\tBase64.getDecoder().decode(\"" + rawEnc + "\"),\n" +
							"\t\"" + defEnc + "\"\n" +
							"))" + (isLast ? "" : ",");

			System.out.println(entryStr);
		}
	}

	@Test
	@Disabled("Just a cursed Helper Method to generate the map so I can copy paste it or regenerate it if needed")
	void _generateKnownValuesJS() {
		for (var type : crypto.getTypes()) {
			// Encrypt String & Raw & Dev
			String strEnc = crypto.encryptStr(SAMPLE_STR, SAMPLE_SECRET);
			String rawEnc = Base64.getEncoder().encodeToString(crypto.encryptRaw(SAMPLE_ARR, SAMPLE_SECRET));
			String defEnc = crypto.encrypt(SAMPLE_ARR, SAMPLE_SECRET);

			boolean isLast = type.equals(SymmCryptoType.values()[SymmCryptoType.values().length - 1]);

			String entryStr =
					"    [\n" +
							"        SymmCryptoType." + type + ",\n" +
							"        {\n" +
							"            secret: SAMPLE_SECRET,\n" +
							"            strOg: SAMPLE_STR,\n" +
							"            strEnc: \"" + strEnc + "\",\n" +
							"            rawDefOg: SAMPLE_ARR,\n" +
							"            rawEnc: b64uDecodeLocal(\"" + rawEnc + "\"),\n" +
							"            defEnc: \"" + defEnc + "\"\n" +
							"        }\n" +
							"    ]" + (isLast ? "" : ",");

			System.out.println(entryStr);
		}
	}
}
