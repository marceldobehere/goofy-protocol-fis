import {SymmCryptoType} from "../../../../libs/goofy-protocol-core/crypto/isolated/symm/symm-crypto-type.mjs";

function b64uDecodeLocal(str) {
    const s =
        str.replace(/-/g, "+").replace(/_/g, "/") +
        "===".slice((str.length + 3) % 4);
    const binary = atob(s);
    const out = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) out[i] = binary.charCodeAt(i);
    return out;
}

const SAMPLE_SECRET = "AMAZING INCREDIBLE SECRET FOR THE SYMMETRIC ENCRYPTION";
const SAMPLE_STR = "This is a test string! This is a test string yes! This is a test string maybe! This is a test string! insanely long text! AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
const SAMPLE_ARR = new TextEncoder().encode(SAMPLE_STR);


export const SymmGlobKnownValues = {
    knownValues: new Map([
        [
            SymmCryptoType.AES_GCM_128,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.ZHo7j70Z6sBkUOH6qkMW7JrzFzkzMt-T7cFMU1JBkmK5CVT7ABGPFDfUMaYuOjCL4iktvBQ0CUnwgpcvoZ5WX7eZ2q1MVZUZrD_q1qAqwCpzBGy2UfMgJ9D4fKvpyqctyI6HjfCpIfOA2spJ0o5g57xUp82GFBdbuTH-7fXxXF2jv2gvXU7M68u4MXFhpnJ-YVBp8eiNoy_AxW6gz3tb7PJnjvrWbPpTa-oU2Smpk3OU1zIxupgMXfAZe10o4oc5N4AaYfMJCF1c3hacAMHzH3Vj4Y60L74ANygDCekxwdATTuDMUznvo9pccw3rXuJBGiz-ktnH4wWwYuQ1G9cyAXTMLL2FSX73ku60uNhJjHyU6p9kfPdMHKuH4gQQnHjVrFf_atS8NfXLB-saEXfkgZ-dz_3XDJKcvo_s8rHuddUCTAo1LnMw9T3ZhSOkLC3ZopH6Ms9b169wNIrs4vi9COy6D5M3OcWnbOaJIqX4AS1rhbKSk4qqfr6bOOiwRLmj031tFIdZZSyr24TOuGADPXpkrR0WN-esYLEKDC2XTB-n97O7liErqmXMY1D5VcrwOg3MF3F3QLwlzcZAfp8gq7OET-jm9SA=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQK1K5n8h0yHNbufCwhKh8/qWzLgkMwoH8uhyq7r4mKZqRr/PpCVURefmVMDgHEuPoATyAlBMqkxRDRjYwG7mHUfdq2PwVYFQejDHAVZHtYIEVq+srR1IPAXhvIlvvbofJsix80NU7geTGZpCBecigo/sVe39/bgvRlEamdFN+Ri1oiHNaepx8kbwq1DAklcNHoD6R48nkss1IhCd2Ib6N9ttXJDqOlfKVXbgOx3x22DOP8PW+d5agdbLAVDgCLo/jFClhBnOj8w29voBv3bJ2R8pBOy8Dv89YvqnDT9Hr0V19PKa9WqUOfm9YdId8BQvlskvrcjTDy8vNDo5BhVGRcLkESWPGVKr3SV9bmnH05j5+hkZdp5k2beA+B3SopHI4GG5+9YkcaYR+ylMFq3EVLY8wIfFA8BX8Mb++zKysI69JJmxbH5JSR8cQORIXyxzCN9Hug++Jfm7KoDBb8AMQFmLmQNJxbAwpjOFGcjK4cMVXygRwWX19ZVf4y+Wf7YB77015eSVAOvLElVLkPV4FaaWEtSqT0YTO0InWYgsCNEaB3o0cKmWo5jMfY/0QuHVX2O+RZYoNXiYT860pUZyecPYWOQQvgE5Q=="),
                defEnc: "AES_GCM_256.bdQUXvNs5XjzwC9Z712Nw_crMWdm3_H0e4bWzxs2YQWdijxRgWPztyp9-WHs7iSlEvp6Ff9S8f2vPxPtm0gHM_4HEJak037FMCScC0xQO5fmldv953weI3pG409gLMPil_EmbPl5mn679XNdlJnsI_suByc12u_1JIdSjLhucRcuXXYWI1HmpQSJ8IMLyjlijj8_QgmnGgNNRTCmDUD7XewAuIrp-CF87yDLDIaDgOiG-SxvOJNjURazRlJkiF-8q-swczdSmefr9IlwMicDpmL4xYvYFfRQ-Q9dBML9IfFPg6BcIAC-7x2kvPMgE1mdoOSx7KoEb1vZS9r0RPO1Tamv68X2va5xiGXuTe6UP3FT79RCptOjmhraNkQHs4-C7pFXCyOMabac1EBdtsZVOiStUnLybxfNHSmKj2-2O_5UUe2-abOjD10yyOXadPFdZj-oggR5ygl7rJ6CZ-2F65nVY5wLRwIk7Z-HtxteD22VJDD48nOE7WNjWD2qtAPxGy2Ycc9bqiWuVzTncRm-CayiOuLEQlSpa9RFrHju15Yy3Ij5bssB7MFQ5Tw-eTCaNNDZSrf8ndxrdAWDE58E8q8w9nPQiDg="
            }
        ],
        [
            SymmCryptoType.AES_GCM_192,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.9xItatXCPqVwvfHPv-EH49BCk-tdxW7bsa8NzLWl8I061PEEPoHKTO9DjBqbL-ZiGSl1sMKKdGTjo8zCejkoGCvHKamble4kSL91bZYMQQWVVcUBH-QlzNuUM37d1cmt_hqi03OR_arth6cE5tqZxxhEB9p1vNP6uM_DTzf5OvFLIV_arRRjRYn6q2KrG3gHHnnw_5QHnncjM9B44g4Hx0I5SMeE8-fS9vy_MXjcJu48NLDkyjRSJghuCqD9CruVGYOx9R2nHFbWl2wRP0cWKpBNcGEBLzQrM7O1aR9TP0c9Ie2oHRz_gIzaJrEmai5ZyHRpjaZb-dcRsJmb87AhnVcfURyg-kXqjQjyggBh8vBNW8is-tEWFOZ5sWkkBebhq4m4cZp5dkgnt5YPUVDCV-IZeUN0QB9sM5w7kXtNvLT3_mq2XNgwM3DL0cQPHSz2LMfI2GColnlcNkSTpiprvwvaLctJepAwYZ40JjEBCqCSAznnqSAUZD0OE80oED7KK1D4VwXAkduOT4NKJTzGX9fLhqVVpcLLU25GocQIVSDFazm0G4H8xhjc-C8Cz1DzyIzGO8A_U9jXm4NE0YShZ5T8bpaIlLg=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQIO8l5dBZNS0NJhU/i+XyN8Yt1/c01ob27U9JxoYwYqkrNJWLcZT1xC4vzfbeCHPgizSGMLdawzW8tyUGInwboABbamb6sOwZWqUlhEt1muKqP7KbhoO4QOCYpqQPm4EFeEW5ocJaah3GjW//3EjlTstr/Yy9R+Iuv8sIuY6JblLxq06XT2PJo5B3tkv4SrAbDnx7FBE037BAdIx/oCggPzVNshNL6VnOY9wDRTLSjflJAficIcTV5ovgEN5EWkcg2bY6JOaysXV0Bt3c33N1kvPuTtFkdo8KeLmWBQWjtPx1T5xOxCLSap6n2bFa+RfluANW/JpwbOmwhw7YXBousd5f9AkqE7ZgjpGgsm2Flz/2w+Mefj4wb3a7zvziQDlnv6+eT2SFsQdZ6FDWFG7AqXS9GFNHERI9dFQcR8138cD6GuwY1krYTEclfXaQYAGj57hoZofsUofAoDXhzezm0fkk1TxyeA4BAr7PueftQ/wX74QMjd9CQoNKbDTH67PE1+oxFHORLgIaOxpUKz1stKchhi07bYEoAArNyUL+WHB+KhUkuMThvQ2ShdaOYnNZ0NbgeC3ovTt74oqGax2Oy5NiarCfiwaw=="),
                defEnc: "AES_GCM_256.IqFRvjqkczx9sB0cwGHYA9d4fCSqeWURXffV_y1BXBPaW_eKhVy3utBbWBLuJPO3KOFFkLnK4fHp_AjaT9CaSaYNgctm7JaKyJxTJi-n9P_R_TYMBiq7UUwG-5MHb0eGPmFszRhWIFXyVJjtkk3ZLzssdkTw-MkAy4fDEj-0wj1D6mlAKP6SzTw0uerD528h-LUu_rR89xmWFDXpOiBryu9I2ZlRmFn5gsdQHwcLCvyecI15ilX0Z9Bvn3_fm_R_Sxz0VzmYXfcHt81j3H7SGfq8kkwSm7odLuts_GuGbpVGfvcMNiHLWGYCny_LHRawac3flVcTEnla3PaopQ_ARI5aWAwBvf3h_T76aFu8_VSOMUEUQzf-w_KgDRl80gCIpzJ7gRsiIOxs1hBdvJP_xGuiXxV7qbNWRVy-3yYqH4Od4EwKvY_qFoSPbk0U2E3mfhOcnnYkmDlwGqVKdLi_RbzV-V4BZWMh9u--u-x6HMKzkPfRHWmNOF_3cS-aMpeuZ-bKxsxk6GbG0UYnJcwqE-FuIB2ojPWZ67QLkjULsJ9rFunGRqZ-VYgIoM5m-P3JNiLhpKY2ImaPje71Y4LdguhrBaV3ab8="
            }
        ],
        [
            SymmCryptoType.AES_GCM_256,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.OIVwzMrgW1weY0UxUmyyRltCHAsn0myh90OYwggIAFHU-89_gZxU41eZvLoenUiLD1OUX9qArbMfOC9pz_evkZ9RNMnhcZiJydB1LekuK0r7qIfbH-COS8oddA8MxQnSz2QXwMSkX_HRoLvn11R1fnv3Lce_A7g56c15ZaQ-U2IaKOvhosoEwRlRvJd8vlvGuWn9HLcUSCYahBgGhNXlWucQ1DsZJeY6TB2yXHgGUTXflYziGS89ZN8z6GvKZ73WKEt-p0G_uMijzSgNZ0ynw80noA4ydrAK0zfI72XKvuazi_c0-hcOuN0on7BGVUyyR8PznMhHUhtkXUM3PKx0N9eZESvfMEd00oPVHqrxoMDmOsHBwy1Ijdqyx1SSoNlFJx3vV6djS2ibbvGFnfu13z5tD_KyyjDRwSswfD0R6zcpYTCqbhibrZGo7do37Snre-hbo2u_yCeefnz5Yi8QHGYgeDytCqAVCQPHrZes-YlKLTOU11SgTz-u59GGJeqaayiGWG8irGLimBRz6NIWWxbPL215bffulVhUMdo2cSQGAqWUQONIJy4pw9rfWGHFZoezHHbYnQaIB2iAJYcMlYIowpw8JAs=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQJCo4FCT6n5INyzGtijjEen3U3nrMdr5arsWKkR/C3cbwGsd8a4YXit4VWqp6MExBYdGR7TjwnmtZSEtOAamZvqDEHjZp0mFayQ+3lGBXCxag11GXQ/pgMoQipVo+2z2UXdHspfsRWLc9YyCyXPVOYXuDRFAFzVEfVEXc9SIkfFvCiYtJyajZaRbszPiVIRMPNprM8RvohXt555wIYMj1NUQ6h5//RD43n2skLGvp8uv7n+rJR2ghD1Iv1m2rFBo+6+vpbIW3LuQZEwxdwz3ue++jc9DUZQq89FUMWfuiIIaM6Bz1ROugNLYy4ikgzkErSGeuOc5v7EVjs11STY0kMzeb6Gftp1nA/oBDms1JHOlrJoJFIIclovn1/KOCA6GgtNc5Q/JSFCGt7v1k60uicQK/5GcOYTnZNlEwR0mBzkYDF1oPN+dVYuJExKFGF017nTNXQNSThbCQKJ71atzxVBN78+aunYYCUbFjkpUWcBu5yHksysvUCSh/k+mRMaJ2qfX1vYbFCI19lnnVLKbgrkM1DQRE94vZojAPUQHYa341zSzLVV1AlU7DZEK0pcbC/iD6+DP8UKXAKelTb7mVMHHy/7V30wNQ=="),
                defEnc: "AES_GCM_256.kRmX4QnZvbrTPUlJSDYD2gTIljcXFHrKoyjWX86lEF4S-Owh4pqO7WCI0swiucebOrroWQyNR6eI-FOFEfNmgi39MnvdIY398Xcjl9ddHfXvw1KttIAn4N30DZWGLouYC0tmb41K7riVc9GIbeJyYaOMjBtTvMyXivFTDyHn9rO-RzYium5w-J6R_3FkbowziN42LWM1wMIpvX1e_TEdvlFIanV_uOJ2ygnVx8S_9tLdvziHxn5H4DSFtAe-6XXEWN13ZyuI5a5p7qx_omaHNuJwoOd6HYZNuCBIXeOKbG9Ns8J4zVabtAdTyAp_RARtB1C1Wly3szC4dhjhCDXPWOZrsfAwnUqpFc1rSQcc2lRbDNKfZf7hS1s4revKqWu6DAHj8u8rs1LFOkJy97kKSoYd9GG-UThodTyzJYWwqfu_EHLqBymVjCQlW0OYJh6JPQmQk5lLqoHzj8O8K6N7n-aJA3dP3WXvflG1y6jZ_Gl1HkLK68h7iNSfdsQntrKD6SoceR5Cw9ErkB4Gl104GgXBrTfTToZkzR0yWY90FjNhoaoSP3ZqptVzjE9O4JUmlp2EukwGQQYSjJynGO_hahfqZ6U7ebo="
            }
        ],
        [
            SymmCryptoType.CHACHA_20,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.0kGWgwBTtyGKEE1RxJ4u-xEuZDidkgO5cTl5M3Uxs3gUAI_oN6GQBzx5cAo-jwqwixxWlcQVR5__fXmC2QQjDdq-xsnwlTUM3RMxz6rKQNVu2_Hq88Bn8bz5Chk7utnAUOVEhjaKRvRMT7h4ZAYSVElOKQDCMWmWLOBamBaLHyzAegGHlVJdOGhkUlsuB8y2mRYRxIOOGXsEhgN5XbunmAsjjEuATFwPo2O2mF_9mbnFOooD8C4scY0iMkCxB5ADSK2PNLBg3rJuvBJVXog_Mfl5AvfGI3QpDU85ZaLwpvNejhR8WaqVvI7dS8TXbsHhUjnQpDXexRAjv9KaRCX0Sq0ITmJUfsd6UCzYxt3_vR6G125x1_FhEH6CPjQVYUIn0iSInuN68DvHJmjbIUOwKPzUY_hfrwZphB8GBbkLO-9B-oo66dSFYoXo1BdajnJEFkp6Wc5OjfF2LwnkCVeSwqSjF9-oLtWZ7lH3Kawx-jerat9tGhblXKQ3VCMCmLmnVvM7xX3ve1RM7Db5G3c2DnGj9fdem30ux0PDDlpPXzrXuYj8v_JQ0gqnSl3TwgSkl3xFmLYBZqWehu6gvZ7Hn-Qb6nzdU6A=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQIl/wlXMUEU4R3Kbbi6wfOfWlIHGuWvkICyXv3jdppcnnfI4EPGZMjerDNMv5fneVa+hZmAWVmNxSfqilANbC7wPvR7zDNVKZPwNT8mqZcBM0R36Nfe90wfB1lCZ9ULRLE0yU4meq+GzGWvjwZcE+FXIWUspIX1rFqnv+3oD+mwntkMrNxK7j4Im1drFaDMIAlvJvwHc4/q6f5AuqmrORe8HVV+IGmSx9nAxdylpICV0IdM31Eq00RrAWiZ6eWuf1PRgMNmq7aD4Jf/2+yo9qrAfW8hd3/93ApBU+8fecWLdOPAOOLYJoB9yvmrMGqR5SnwMgEVAd8oYgEfRGX575S1Uf4JJpI+6LsNOeHOZeE+mouDVYmwTKeSM5ll5BT0E6RzkOsQue5cvZHnyD2bneglCYfArj/qtZyyBATSqETW92niiB8N7UWCkdfparg5JgVzgtlF1EK5BRg56fbFrWtZw7Xb4VmECmeI2rrKeZKwWjZuhx6HuegR+B/yOXHhZvqQP8PKGbGnUmguMtp5qDSYefchBbsOVkNivaBaWKgRAJ2MpuS1Rw4yqdETr9jz8Q0q5wxUAPkAr/IfzOQFguQsVw3+H6ALBQ=="),
                defEnc: "AES_GCM_256.v-5CIedPJuxWbjfafeWluCgCiF6RPvqlryofwcllwUED6HZ9Yn0L2P-oxikn4Gl9l9Xvl9sQPOHM4mfOJ2iY4XYLVLWrZqJEMCNBNjYlrm-7ER2WX9YAMxi9yOZEEQx3_x4a3KUM6d8LovUllboR8FXuiUouDPOqRkn1eaWwSjDzrsOhGw91PgbTrnHGI-9CHa83MPVV4-_X3fqP2BfaYUWs0TIfNLZcCuLEQtJGNEaAiOgXXK9Ooya4U8D9IVU_qnI2VTlki7dkvIf1xGtkg21l3TBb4aGAQl1YH6uBevBS6Jxeq9HVuI0PWAAN7W3zfqRs6KP4FB7nJCw2SLij4MLpx6yYwwRD5BUGnLT4JYeU2KFNXHuFZCtDXWaNGwPWGwHMaSmKLKea3ZCPjO3Av6WKRLPm6xq5T55phbtYxaTjMFQh561ZbW8HO5Q9pRoAn7VBQGlQpmerQtvcwtbEfO2TziAMPmeuOvxvZ81tmk-scVqlUQC9bWu7KmMrk3_k1DU1Fami6vlv5TFzEC6GBP0RcMgDVypz6dfaLmAggL1pkG9S0ZMkDo5kLNThZ_Te7UdeQGHQOag0G5oYXDyU1RCLV9J6ZjY="
            }
        ]
    ]),
};