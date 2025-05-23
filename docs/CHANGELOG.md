JSON (C) Black Rook Software 
=============================
by Matt Tropiano et al. (see AUTHORS.txt)


Changed in 1.4.1
----------------

- `Changed` JSONObject.push(...) and JSONObject.pop(...) should add/remove from the end, not beginning.


Changed in 1.4.0
----------------

- `Added` JSONReader.readJSON(File). 
- `Added` JSONReader.readJSON(Class<T>, File).
- `Added` JSONReader.readJSON(Class<T>, File, JSONConverterSet).
- `Changed` JSONReader had some tail recursions removed - should accommodate larger sets of data.
- `Changed` JSONWriter now skips indenting empty arrays.

Changed in 1.3.0
----------------

- `Added` JSONConverterSet and JSON conversion options, allowing the library to separate how it parses JSON from different sources.


Changed in 1.2.0
----------------

- `Added` Additional JSONWriter methods for output options, plus an instantiable JSONWriter.


Changed in 1.1.3
----------------

- `Fixed` Reduced memory allocations in JSONWriter.
- `Changed` A slight tweak to member gathering.


Changed in 1.1.2
----------------

- `Changed` JSONObject.create(T) will return the passed in object itself if that object is an instance of JSONObject.
  This was done so that JSONObjects can be generic members of deserialized/serialized data. Discussion will need to happen if this
  method should deep-copy the JSONObject instead. 


Changed in 1.1.1
----------------

- `Fixed` Array types are fixed.


Changed in 1.1.0
----------------

- `Added` Generic collection hints for erased types (JSONCollectionType, JSONMapType).


Changed in 1.0.0
----------------

- Base release.
