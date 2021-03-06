(ns bjitcask.core
  (:refer-clojure :exclude [get alter]))

(def ^:const page-size 4096)
(def ^:const header-size 14)

(defprotocol FileSystem
  (data-files [fs] "Returns collection of all data files in the bitcask")
  (hint-files [fs] "Returns collection of all hint files in the bitcask")
  (hint-file [fs data-file] "Returns the hint file associated with the data-file if it exists; otherwise, return nil")
  (lock [fs] "Locks the bitcask on the file system")
  (unlock [fs force?] "Unlocks the bitcask on the filesystem.")
  (scan [fs file] [fs file offset len] "Returns a seq of byte buffers starting at offset of total length len. Defaults to scanning the whole file.")
  (create [fs] "Returns a map containing 2 keys: :data and :hint, which are each random access files."))

(defprotocol IDataWriter
  (data-size [file] "Returns the amount of space in bytes used by the data file")
  (append-data [file bufs] "Appends the given bufs to the associated data file. Returns false if the append failed and a new data file should be created.")
  (append-hint [file bufs] "Appends the given bufs to the associated hint file"))

(defrecord Entry [key value ^long tstamp])
(defrecord HintEntry [key ^long offset ^long total-len ^long tstamp])
(defrecord KeyDirEntry [key file ^long value-offset ^long value-len ^long tstamp lock])

(defprotocol Bitcask
  (keydir [bitcask] "Returns a snapshot of the keydir")
  (inject [bitcask key keydir-entry] "Injects a KeyDirEntry directly into the keydir.")
  (get [bitcask key] [bitcask key not-found] "Returns the value for the key in the bitcask.")
  (put [bitcask key value] "Stores the value for the given key.")
  (alter [bitcask fun] "fun must be a function that takes no arguments and returns a key-value pair to be `put`."))

(defprotocol IClose
  (close! [this] "Shuts down gracefully and frees all resources associated with this."))

;;;; Global Vars DONE
; page size
; header size
;
;;;; Global structures
; entry {:tstamp :key :value}
; hint {:tsamp :key :total-len :offset}
; keydirentry {:key :file :value-offset :value-len :tstamp}
;;;; Global functions
;; DataFile
; - data-size (space used in data file)
; - append-data (these distinguish streams)
; - append-hint (these distinguish streams)
; - close
;
;; Codecs DONE
; - decode-all-*
; - encode-*
;
;; Filesystem (created by open)
; - data-files (discover)
; - hint file (find companion)
; - scan (read)
; - create (write, a datafile)
;
;; Bitcask
; - keydir (returns chm)
; - inject (seeding the keydir)
; - get
; - put
; - alter
; - init (takes fs -> seeds chm)
;
;; Merge
; - process (uses fs, codec, and keydir APIs)
;
;; Registry
; - open (uses fs, keydir, merge)
;
; Closeable protocol
; - close!
; TODO: use records instead of maps everywhere
