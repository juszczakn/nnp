(ns nnp.file-browser
  (:use seesaw.core)
  (:use seesaw.keymap)
  (:use clojure.string)
  (:import java.io.File)
  (:import java.awt.event.KeyEvent))


(def default-width 600)
(def default-height 480)
(def open? false)

(def current-path (str (.getCanonicalPath (File. "./")) "/"))

;;--------------------------------------------------

(defn seq-of-files [dir]
  "Returns a seq of files for the directory given."
  (map #(replace (str %) #".*\/" "") (.listFiles (File. dir))))

(defn list-of-dirs [fileseq]
  "Returns seq of all directories in fileseq"
  (cons ".."
        (sort (filter #(and (true? (.isDirectory (File. (str current-path %))))
                            (false? (.isHidden (File. (str current-path %))))) fileseq))))

(defn list-of-files [fileseq]
  "Returns seq of all files in fileseq"
  (sort (filter #(and (false? (.isDirectory (File. (str current-path %))))
                     (false? (.isHidden (File. (str current-path %)))))
                fileseq)))

(defn list-of-files-dirs
  [filepath]
  "Returns a list of directories and files in current file path"
  (concat (list-of-dirs (seq-of-files filepath))
          '(---)
          (list-of-files (seq-of-files filepath))))

;;--------------------------------------------------

(def file-list-box
  "list of folders and files to select"
   (listbox :model (list-of-files-dirs current-path)))

(def file-path
  "Current path"
  (text :text (.getCanonicalPath (File. current-path))
        :columns 1
        :editable? true))

(def file-name
  "Name for current file to be saved/selected in fb"
  (text :editable? true))

(def save-open-button
  "Button to open the file"
  (button :text "Open"))

(def file-open-panel
  "Horizontal panel holding the filename and open button"
  (horizontal-panel :items [file-name save-open-button]))

(def file-browser-panel
  "Panel to be added to the file-browser window"
  (border-panel
   :north file-path
   :center (scrollable file-list-box)
   :south file-open-panel
   :vgap 5 :hgap 5 :border 5))

(def file-browser
  "File browser for viewing directories and opening/saving files."
  (frame :title "File Browser"
         :id file-browser
         :content file-browser-panel
         :size [default-width :by default-height]
         :minimum-size [default-width :by default-height]
         :on-close :hide))

;;--------------------------------------------------

(defn save-file
  "Function called when save is done"
  [file]
  (config! file-name :text file)
  (def open? false)
  (config! save-open-button :text "Save")
  (show! file-browser))

(defn open-file
  "Function called when open is done"
  [file]
  (config! file-name :text file)
  (def open? true)
  (config! save-open-button :text "Open")
  (show! file-browser))

(defn change-dir
  "Change current directory and update filebrowser accordingly"
  [dir]
  ;TODO fix to make empty? when no longer using '---' in listbox
  (if (true? (.exists (File. dir)))
    (def current-path dir))
  (config! file-list-box :model (list-of-files-dirs current-path))
  (config! file-path :text current-path))

;;---------------------------------------------------

(listen file-list-box :selection
        (fn [e]
          (when-let [sel (selection e)]
            (let [s (File. (str current-path sel))]
              "if a directory was selected, change current directory else put file name in file-name text field"
              (if (.isDirectory s)
                (do (change-dir (str (.getCanonicalPath s) "/"))
                    (text! file-name ""))
                (if (.exists s)
                  (text! file-name (.getName s))))))))

;;File-path text field key listener
;;TODO add '/' at end of filepath
(map-key file-path "ENTER"
         (fn [e]
           "Pretty up the input text before displaying it"
           (let [path (trim (text file-path))]
             ;if path has trailing '/' use that
             ; else add a trailing '/'
             (if (re-matches #".*\/" path)
               (change-dir path)
               (change-dir (str path "/"))))))

