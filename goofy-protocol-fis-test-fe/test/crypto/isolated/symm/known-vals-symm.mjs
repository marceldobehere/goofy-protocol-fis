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
                strEnc: "AES_GCM_256.97r3yKfBlp_Nx2jq2xeFJp3KyaRcHBOgb6Ve5e-n4mLPo84kzxrodvaQwF5kLUF8VXCZhmIn-mL5zn_VH1Sx6lVdcjS5CQHHS_1-5qd_JHMaHuBPoApGlHCiBJy6baohGDW64S4l31mMvtRZSFdf0CkfmrYwkcQn04Hd8DUKZze2jTUHpuKhJvbvTxicGwjiIKEZkriafy1e4ksbAO7_s98fP3t_mU1xF102pmXHx26EW3AjRFjIOjlalvyegLuSaC4yhDVHhAvSVCNfY8dkLktuN1vxJfkLIShPh_Q3EGOqc_9bkanzZJfoWb4jjPsRvu1UXbHFE9JurTffSSKDSrroEo13gq67ZWGGw6pA118r_oQVK7RDBa0Vktpohn1en1-LZHIxCgeEsI4vH0f3g439V0iDjNhN9Sm2tYi7_A979ZcUDumDWtaH0CjKjwNsim1LmIHHtlFSssS5h6JirfKaS92vJDT067kQPnpaOX--NFJNTHvcIbYH3Ef1MEi5RzBN7AXIdoi4vwWUCnPiQ1SMrgKZspB5pzDfD6Uf_e8Zx3iM1FYPwSVmBe5VjA45AsxVNetIVSZl3lwVFthajSujgs5FjWY=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQLBawZgIL0gXEMN+rzLbpQqlzlOEaVOied4WwfE+bEJHoFWT/wGGHhhO+zn/b5xmy46MJsGdzuqfAKFSfbRtZ/qn+TWVU1NYI8e0OZ+r5M6q43V/eBu5cz+vIJzVhOKadmGE38+R1hrPdccZGnLISvV22t29SrjnEmK+g7cmRx8VQ7ExsTmhs2VQID2+e2HKNv81Bdd5l+17J58hgLYQgV5n7/wIC4gdud2lxzlCFN2KJofgc9oBeEpjd/GEbTQk/koDMG9PR31treiTEg4745R39SNBkuy1h5WGSCixesO+XoQtMSRAyeQenlRD7CAFx7ZKB4RB12VjqrkHLesVJzoHaKc4QMo/4Dv4mzQZ9aEWl29sglHAghs3w6lFOFsB5GIJLKpYw7AUkv4KvryAOoJq8wunu3n24JHbAKawLiscfKA6yUysR8UFwJBIeNj89u1d0lf3QLr/W3Bg8PAuRXh1KvPeoet/I7GfvVgyqLDF/r11rsUnyTbZt7uteYOABWANbQDkoNktcvabWE0toMFtwsBmJ9MUnTyx6kBqMHdaly9nmo80UrqzEyfuGLJrUBbBSI9iwoUusp/dKPpdFOJx4FCv6G4Rw=="),
                defEnc: "AES_GCM_256._g69DQvtFbBaX3Ej-u4BmcYotJDMDgKjlZk-H9H-11SAxLi_NWBDAAUuN7oGSLfQIhifbsPHzIBgClc8cqx115GrXy58GLZw890kI3-sib48uz5-rDskE2eMM8Ur6MfAh6E6kyKMJtTlPrIoBUvODxBQbu0rMLgRDBpwyxa27sg4_kCKbhpXtoAGwvUrS-hUqUfHG2ZXAyBzDLq-O77kThV4Nj7e1aTD1GdPFm_BTHV2PkvrVZL8Qx4OsUu90uqh_6yn7dgwrhDvdKtQ9aKiiVm27LoUPHwNlMrRFtFvni7La7j7ExS3cguIM0nbtmCUkKpHzVkOFLqrM4QnZDFaWS5WUSR4bpiQOn2o_mwwLJeNcKigLCckHp92Afre_O8rYqzcZ2niXxZPeTdW8yJNVEeC8W29poUD19In7Kg5gvsO7hJBg9CD57_ICBll2GW1QQTquz4S-cn4e_IY-uGXOBvONAQeyj9hZ3qiLKIg-VWYU7xY6I6-SQhtcOqTGu3hlD3onSvbx63snO33x5QyCMiHl34VXkFtYCx-q4QqL048ey3HWnGF1yYTgd53eyazrwU_wA6eQLHlCv7s_7PBn_ClAawHJeU="
            }
        ],
        [
            SymmCryptoType.AES_GCM_192,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.HzdymNlWnFO7bY0Yhq6-s4wkLVqyfyfGK5RnoqgnoV3TZWYioQj2j5xsR46dD4eW-vF5URGXgwaq40RRolQqZCroIr8S5hSsYBbg6KUlTqLBUnZBJL3GhY7245Omqym0ytfArdYI1LMED3ZTsP6ne7xjOyUkhqFX0O9jqqYaBeyH07Ow-vL-Zt99fTfnEHzGZbfcrusAB3laE8vErskz-7Xm7--eQyLSow2Unw6PEtqAxsdxj6RQfBupizGKZenf6mPdcIC_J_dJOlAV-yj-BwYJ_4G7B6XtNyFoRo1Fo1PirftqaxGvzbuyyJGdAZtVmMS24zrdgQkW6YkT4ZKl9ImKxG8YQiCGgA8GDzm6Yp7YSvofiguniBoDj52rI0c7rvINKoRyygO0k5huAS5VLi7-72-zl9RvwnFh52WFmjwffqfcH15KaYVM5PAeSa_GQ8pxsHE2zBnu4BS0kAXn0DeEN9ll_5VYlDoTKvaJ_HMvSekRM1_2wgBdMJP8uJECc66klHuJxwdOoXT9ZI09c95LTC2s3_B52vlXjAcJFfuIPUlPjW6vsGrdzbK5Eg4R8oRydH6o7W1rzSXa-DsBNhNgxPHVNWY=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQKKl5ePoYxbBnT0/oVkTcgnDax5v09JTDryy6NTiv43pZnplTaZUsv2BSJS8uv0++eg6bQt2H6q+Eq13rGQGBZg7G94MobXbtup0a8Fv9MuFho/KXt46srdEZgCSM1ZrRYp+7HxWL4Lksm7hBCWji8ZRzbzy/wiSiBAHOD7kJT0cxjruVYZAH/UMp8FX4+u42bIQSuLjbxVkozAHjIASzgd/5XmlpFib7SBuxHLqhDrB7Ew6V+dWrHAeAs4u0Lm8BsGcFeR9EsBvnpcFJ+LbPfx0C4FWoZIn1J5mMyBvYf63Xy+Aay76vWz+a5Ks7ijdRpZ1ocpwJ6eCBVerJ6nwzUmbmM+g4puE0qwqcGBBgL/rlVd8DNMbB4GJX7cqpOatEYDKqjqmMTlHGA0TjBT6iU1gLiSqvH/ns8no50iAQbpuXy4OR4+3dtnV+aSswBQAim8KPgmH+QcUpgL5Tul+HFD56Q+cV7x+otheYMEAoqbcR6k0enBVHOF84C91h5ZUKIPhRLgp6/8OyOKYdR1dEoG9M/7oyou1f1C1Dx9ALZASEH+1jivqf6uOHmzAcYA5zbypTBcNaKjkPYKkIv8G0JLyIZTMR7rXg=="),
                defEnc: "AES_GCM_256.E9vrzS74p2RDf3aXaEutiE30pg3uIns9YT7is-0zPjT5Ac96FoZEhdp1wqUJ03BAP_2IKu5J-adjSliqNEx78PBzZUn8osuOC_g68hp5gYdZNWu4hI-g8_8hOVehbdwHtCbBt2vqj88kNtA5bHm_AgQiH29BoENIUt9VY4y_IHtOurPTUMoaEfFcRCVB8AdXElCUiK1PV2JJv5xJ_hGy_rmm0aVGhvgcinYjIiTg4K52uYk9EM_RxrowZBhvcdkhAd__isv_RONIYpsLc2HH_WbDFRYiHYl30N9OeMIIwikSQ6h1DSOa4DoBdO_Prg2ppyls4LQevD3YY87eGnpHJM6rHJ3Y0-ZsAR1lIlDXjgugiX2vgGzpC1fjJV8farcORfKA08n2i3AKbhgDgoqLUtiVuuhiYbBj8DipSr32uXZa-G4xPn7-zeKqJgFgS6ICJmdKibn6S_Lm6A7-jy5b4BsjdMLHa88NeTczaf9KRqm2K-Hh1BUtKbwAugdUq7fBC2MoosqjkwmP36OWTK3Cu897IAVoAbVeYaFMzrbvTyW6W_Ohhz-QFDOwOql17s2f6wGHSeIk17QTWjMohHf6wjoGl-pDezI="
            }
        ],
        [
            SymmCryptoType.AES_GCM_256,
            {
                secret: SAMPLE_SECRET,
                strOg: SAMPLE_STR,
                strEnc: "AES_GCM_256.jegD7YPy4iV90idDVCPLBh0wgvXx1kNDdTiawj1eVSPlqNHtaCcejCbypuukoIjQNLYbfPFS-VYsGeEQRW9vgDtOBVhfHxrxT-bJYafSKxLF9Y3w_Xz99fCgd2jk8hzGr90VcdT-2Mqamm4lc6He8Y_ue4ujrHepz75kODSlqVZ5VPookoWguOi03eTW0E8GUhqTuimDpUhyUmQWJRilNXY8YH4BF1ek1dbf0DAJngHJs0EKUnE0jGmJ687d1hbJCcW6jtZ5bnV0crE4IL7F4q8NknoCrIIU0jdbu8hJNfiYpEj5u_2n43U3e2pgzD0sSqZ0ekOlsU0I4lEq2z9n7TFGrsRHJ_sgPEZUZloodG7e3tnC5LlDK88ryUd-q5IC_keW_Kgcp45MZ32i63mAz5asaVRHDPPh5yXKOi3J6yZDNEACw3PtB_w_NY5qtQb405mduXHj1KE_mEvDdyHhY71TlDnFczM5wmyCOy6aM7PV__FFhtKNm0dEv4vCRwSkTMHqveXCRhiOjnBHJJ0NspNajzGU97GcZEz0IMXNyeUnTyJenCiANpoVHezUQuxi6fPoELNFmlt80JsxY_HAF_LOcpLLROM=",
                rawDefOg: SAMPLE_ARR,
                rawEnc: b64uDecodeLocal("AQLvV2+OUKyFUNDlPEJj4MsD5rFKrxkV3cI3HTaH6aZNPWtcqvVEfkKKIXcw6P0PVA2+JtccW525IFNRqcvzd3Clndrd2F1ZwTpspWCHn1Aq7AYXh58z+KYiDf71iNGOsMadH6134tzvsH4ABFR3UmOZnUa0ZiNAQuGnOIR0ZqQpxmwUJ5bSyikmBsm4UGI6Z24ywFo4gQ4PgZH+bl/E+9lF4Ygw/udLtvSXYQSxv2JZ3ppsZEO4YTlTs1A85lzcbJAWYSZrF0l2Jf6CE04b4mbPe0+C3eQAKGT1FdxIInPeEu0RIUhAdljFYUQC7FHk5nairIRkDsludSlyzqsmlPeqMdnRu/eE0zZhV01s4NaJJYYQmFh/H/QkHOSHjsMTsJ3ZXHV4lbgjJ12kHNN4dOLsj5brCr6kcw49vq47J/nrUIzu5hj+VpQrdFcYc8x4DIq7J3p5ET74Dajrn6F/mijPCZxcAKJ5hDR9aFveP44oQG3f/VSmUp4b7mNhqEnOEBYLqAYwWnZN4L+NgqsobJLJgWaGoefo6+jKe7BZFLc5qt63DlzXmyEeaY+earTe0KsQPPYhiel6itnl4GK1beJlumSLgANzYg=="),
                defEnc: "AES_GCM_256.Vvk7jPF30YIojC6rGlMKFmMNRSgONm-2QfhmCCGRcXovZTpL2yoaiY0lNMFnp-Z9v43VKtJuC41A1pZW9SzuvcySJQpQuMekBmL7m9GzateUkbf78EXRb0A24OHrEN4vKRhzGbLbeA19e4sd_xMw1-37JYZHZ-xgxsZbBPu6FmyiLFg41YKO9e9Avl4FS6P1RGUPHk76ng834c418Xrm_NaseNEjTr56pc_4oT3mZ6ZFGqNVJawuJTF1qjm3GUYMQspC6azw58lz8lry7TfEGO2VsjKPdVtDWYgkjG6JyTFGblFQBamiEvX6BsFB05l0U4GEsxGWMflQ_1v1oNj7kiVOasU0wyO8KdESsOhzEQcjldnek6NF5AmJ2W-BM9QCRWQCb4w4Nbyg1pfdxaBgpN1BWUSv1YUpyFZYDv5T3oflRxo666ITxORZujUHdARzY4S-4k2ETbRSOkhPdvAirqMqjj6hjVB1llPZX3UB6tZNkI3JqdzbFxovWm-o9WBy3Wmlcjs58jWbjbA4zdkQdSlP-a7pRsjuozrQ7FVSfY-fdimCarBrrGinHV4YC7flsVZqpxVt6nkYZjVwgyP0QkKEu-hzXxU="
            }
        ],
    ]),
};