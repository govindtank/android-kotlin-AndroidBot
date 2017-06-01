package com.kotlinandroidbot.data.network.cookies

import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import com.pixplicity.easyprefs.library.Prefs
import java.io.*
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.experimental.and

class CustomCookieStore : CookieStore {

    private val map: CookieMap

    private val cookiePrefs: SharedPreferences = Prefs.getPreferences()

    val LOG_TAG = "CustomCookieStore"
    val COOKIE_PREFS = "com.kotlinandroidbot"
    val COOKIE_DOMAINS_STORE = "com.orb.net.CustomCookieStore.domain"
    val COOKIE_DOMAIN_PREFIX = "com.orb.net.CustomCookieStore.domain_"
    val COOKIE_NAME_PREFIX = "com.orb.net.CustomCookieStore.cookie_"


    init {
        map = CookieMap()

        // Load any previously stored domains into the store
        val storedCookieDomains = cookiePrefs.getString(COOKIE_DOMAINS_STORE, null)
        if (storedCookieDomains != null) {
            val storedCookieDomainsArray = TextUtils.split(storedCookieDomains, ",")
            //split this domains and get cookie names stored for each domain
            for (domain in storedCookieDomainsArray) {
                val storedCookiesNames = cookiePrefs.getString(COOKIE_DOMAIN_PREFIX + domain, null)
                //so now we have these cookie names
                if (storedCookiesNames != null) {
                    //split these cookie names and get serialized cookie stored
                    val storedCookieNamesArray = TextUtils.split(storedCookiesNames, ",")
                    if (storedCookieNamesArray != null) {
                        //in this list we store all cookies under one URI
                        val cookies = ArrayList<HttpCookie>()
                        for (cookieName in storedCookieNamesArray) {
                            val encCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + domain
                                    + cookieName, null)
                            //now we deserialize or unserialize (whatever you call it) this cookie
                            //and get HttpCookie out of it and pass it to List
                            if (encCookie != null)
                                cookies.add(decodeCookie(encCookie)!!)
                        }
                        map.put(URI.create(domain), cookies)
                    }
                }
            }
        }
    }

    @Synchronized override fun add(uri: URI, cookie: HttpCookie?) {
        if (cookie == null) {
            throw NullPointerException("cookie == null")
        }

        var cookiesUri = cookiesUri(uri)!!
        var cookies: MutableList<HttpCookie>? = map[cookiesUri]
        if (cookies == null) {
            cookies = ArrayList<HttpCookie>()
            map.put(cookiesUri, cookies)
        } else {
            cookies.remove(cookie)
        }
        cookies.add(cookie)

        // Save cookie into persistent store
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.putString(COOKIE_DOMAINS_STORE, TextUtils.join(",", map.keys))

        val names = HashSet<String>()
        for (cookie2 in cookies) {
            names.add(cookie2.name)
            prefsWriter.putString(COOKIE_NAME_PREFIX + cookiesUri + cookie2.name,
                    encodeCookie(Cookie(cookie2)))
        }
        prefsWriter.putString(COOKIE_DOMAIN_PREFIX + cookiesUri, TextUtils.join(",", names))

        prefsWriter.commit()
    }

    @Synchronized override fun get(uri: URI?): List<HttpCookie> {
        if (uri == null) {
            throw NullPointerException("uri == null")
        }

        val result = ArrayList<HttpCookie>()
        // get cookies associated with given URI. If none, returns an empty list
        val cookiesForUri = map[uri]
        if (cookiesForUri != null) {
            val i = cookiesForUri.iterator()
            while (i.hasNext()) {
                val cookie = i.next()
                if (cookie.hasExpired()) {
                    i.remove() // remove expired cookies
                } else {
                    result.add(cookie)
                }
            }
        }
        // get all cookies that domain matches the URI
        for ((key, entryCookies) in map) {
            if (uri == key) {
                continue // skip the given URI; we've already handled it
            }
            val i = entryCookies.iterator()
            while (i.hasNext()) {
                val cookie = i.next()
                if (!HttpCookie.domainMatches(cookie.domain, uri.host)) {
                    continue
                }
                if (cookie.hasExpired()) {
                    i.remove() // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie)
                }
            }
        }
        return Collections.unmodifiableList(result)
    }

    @Synchronized override fun getCookies(): List<HttpCookie> {
        val result = ArrayList<HttpCookie>()
        for (list in map.values) {
            val i = list.iterator()
            while (i.hasNext()) {
                val cookie = i.next()
                if (cookie.hasExpired()) {
                    i.remove() // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie)
                }
            }
        }
        return Collections.unmodifiableList(result)
    }

    @Synchronized override fun getURIs(): List<URI> {
        val result = ArrayList(map.allURIs)
        result.remove(null) // sigh
        return Collections.unmodifiableList(result)
    }


    @Synchronized override fun remove(uri: URI, cookie: HttpCookie?): Boolean {
        if (cookie == null) {
            throw NullPointerException("cookie == null")
        }

        if (map.removeCookie(uri)) {
            val prefsWriter = cookiePrefs.edit()
            prefsWriter.putString(COOKIE_DOMAIN_PREFIX + uri,
                    TextUtils.join(",", map.getAllCookieNames(uri)))
            prefsWriter.remove(COOKIE_NAME_PREFIX + uri + cookie.name)
            prefsWriter.apply()
            return true
        }
        return false
    }

    @Synchronized override fun removeAll(): Boolean {
        // Clear cookies from persistent store
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.commit()

        // Clear cookies from local store
        val result = !map.isEmpty()
        map.clear()
        return result
    }

    /**
     * Serializes HttpCookie object into String

     * @param cookie cookie to be encoded, can be null
     * *
     * @return cookie encoded as String
     */
    private fun encodeCookie(cookie: Cookie?): String? {
        if (cookie == null)
            return null

        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "IOException in encodeCookie", e)
            return null
        }

        return byteArrayToHexString(os.toByteArray())
    }

    /**
     * Returns HttpCookie decoded from cookie string

     * @param cookieString string of cookie as returned from http request
     * *
     * @return decoded cookie or null if exception occured
     */
    private fun decodeCookie(cookieString: String): HttpCookie? {
        val bytes = hexStringToByteArray(cookieString)
        val byteArrayInputStream = ByteArrayInputStream(bytes)

        var cookie: HttpCookie? = null
        try {
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as Cookie).cookie
        } catch (e: IOException) {
            Log.e(LOG_TAG, "IOException in decodeCookie", e)
        } catch (e: ClassNotFoundException) {
            Log.e(LOG_TAG, "ClassNotFoundException in decodeCookie", e)
        }

        return cookie
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!

     * @param bytes byte array to be converted
     * *
     * @return string containing hex values
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v = element and 0xff.toByte()
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v.toInt()))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    /**
     * Converts hex values from strings to byte arra

     * @param hexString string of hex-encoded values
     * *
     * @return decoded byte array
     */
    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character
                    .digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    /**
     * Utility function to male sure that every time you get consistent URI

     * @param uri
     * *
     * @return
     */
    private fun cookiesUri(uri: URI?): URI? {
        if (uri == null) {
            return null
        }
        try {
            return URI(uri.scheme, uri.host, null, null)
        } catch (e: URISyntaxException) {
            return uri
        }

    }

    inner class CookieMap : MutableMap<URI, MutableList<HttpCookie>> {

        var map: MutableMap<URI, MutableList<HttpCookie>> = HashMap()

        override val entries: MutableSet<MutableMap.MutableEntry<URI, MutableList<HttpCookie>>>
            get() = map.entries

        override val keys: MutableSet<URI>
            get() = map.keys

        override val size: Int
            get() = map.size

        override val values: MutableCollection<MutableList<HttpCookie>>
            get() = map.values


        override fun containsKey(key: URI): Boolean = map.containsKey(key)

        override fun containsValue(value: MutableList<HttpCookie>): Boolean = map.containsValue(value)

        override fun get(key: URI): MutableList<HttpCookie>? = map[key]

        override fun clear() {
            map.clear()
        }

        override fun isEmpty(): Boolean {

            return map.isEmpty()
        }

        override fun put(key: URI, value: MutableList<HttpCookie>): MutableList<HttpCookie>? {
            return map.put(key, value)
        }

        override fun putAll(from: kotlin.collections.Map<out URI, MutableList<HttpCookie>>) {
            this.map.putAll(from)        }

        override fun remove(key: URI): MutableList<HttpCookie>? = map.remove(key)

        val allURIs: Collection<URI>
            get() = map.keys

        fun getAllCookieNames(uri: URI): Collection<String> {
            val cookies = map[uri]
            val cookieNames = cookies!!
                    .map { it.name }
                    .toSet()
            return cookieNames
        }

        fun removeCookie(uri: URI): Boolean {
            if (map.containsKey(uri)) {
                map.remove(uri)
                return true
            } else {
                return false
            }

        }

    }

}
