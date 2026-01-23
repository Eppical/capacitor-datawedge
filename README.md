# Capacitor DataWedge - community maintained plugin

![capacitor-version](https://img.shields.io/badge/Capacitor-v7-lightgreen)
![capacitor-version](https://img.shields.io/badge/Capacitor-v3--v6-orange)
![version](https://img.shields.io/npm/v/capacitor-datawedge)
![downloads](https://img.shields.io/npm/dm/capacitor-datawedge)
![contributors](https://img.shields.io/github/contributors/jkbz64/capacitor-datawedge)
![license](https://img.shields.io/npm/l/capacitor-datawedge)

This plugin allows you to simply gain access to receiving barcode data and use some api methods from the DataWedge API designed for Capacitor with Zebra devices.

`capacitor-datawedge` project is ***unofficial*** and completely community maintained, do not mistake this plugin with the official [Zebra Scanner](https://ionic.io/docs/zebra-datawedge) plugin.

It is advised to use official plugin for full compatibility and guaranteed support, this project was created before the official plugin existed and is maintained by people who use it - ***it does not aim for full compatibility***, features are added when someone cares enough to share the changes with other people in the true open source nature.

## Install

```bash
npm install capacitor-datawedge
npx cap sync
```

The last supported version for Capacitor v3 is `capacitor-datawedge@0.1.3`

The last supported version for Capacitor v4 is `capacitor-datawedge@0.2.1`

The last supported version for Capacitor v5 is `capacitor-datawedge@0.3.1`

The last supported version for Capacitor v6 is `capacitor-datawedge@0.3.4`

## Usage

Enable intent output in your DataWedge profile, set `Intent delivery` to `Broadcast intent` and set `Intent action` to `com.capacitor.datawedge.RESULT_ACTION`

```js
import { DataWedge } from 'capacitor-datawedge';

// Initialize DataWedge with a profile name (required).
// The intentAction can be customized; defaults to the plugin receiver action.
await DataWedge.initialize({
  profileName: 'MyAppProfile',
  intentAction: 'com.capacitor.datawedge.RESULT_ACTION',
});

// Register scan listener to receive barcode data
// The method returns a function to unsubscribe
const unsubscribe = await DataWedge.onScanResult(event => {
  console.log(event.data);
});

// To stop receiving scan events, call the unsubscribe function
// unsubscribe();

// Scanning the barcode using physical trigger should fire up your scan callback!
// Check API for more methods
```

### Example application

You can find an Ionic/Angular 16 demo that exercises every plugin method and shows the
`scan` listener output in `examples/datawedge-demo`.

```bash
cd examples/datawedge-demo
npm install
npm start
# or build & sync with Capacitor 7
npm run build && npm run cap:sync
```

## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`getAvailability(...)`](#getavailability)
* [`onScanResult(...)`](#onscanresult)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

Every broadcasted intent assumes `com.symbol.datawedge.api` package as default.

Package name can be changed by modyfing `DATAWEDGE_PACKAGE` variable [here](android/src/main/java/com/jkbz/capacitor/datawedge/DataWedge.java)

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options?: InitializeOptions | undefined) => Promise<{ profileName: string; intentAction: string; }>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#initializeoptions">InitializeOptions</a></code> |

**Returns:** <code>Promise&lt;{ profileName: string; intentAction: string; }&gt;</code>

--------------------


### getAvailability(...)

```typescript
getAvailability(options?: { timeoutMs?: number | undefined; } | undefined) => Promise<AvailabilityResult>
```

| Param         | Type                                 |
| ------------- | ------------------------------------ |
| **`options`** | <code>{ timeoutMs?: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#availabilityresult">AvailabilityResult</a>&gt;</code>

--------------------


### onScanResult(...)

```typescript
onScanResult(callback: ScanResultCallback) => Promise<RemoveListener>
```

Suscribe a eventos de escaneo con API simplificada.
Retorna una función para cancelar la suscripción.

| Param          | Type                                                              |
| -------------- | ----------------------------------------------------------------- |
| **`callback`** | <code><a href="#scanresultcallback">ScanResultCallback</a></code> |

**Returns:** <code>Promise&lt;<a href="#removelistener">RemoveListener</a>&gt;</code>

--------------------


### Interfaces


#### InitializeOptions

| Prop                 | Type                |
| -------------------- | ------------------- |
| **`profileName`**    | <code>string</code> |
| **`intentAction`**   | <code>string</code> |
| **`intentCategory`** | <code>string</code> |


#### AvailabilityResult

| Prop            | Type                                                                                                                                                                      | Description                                         |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- |
| **`datawedge`** | <code>{ present: boolean; enabled: boolean \| null; statusRaw?: any; }</code>                                                                                             |                                                     |
| **`scanner`**   | <code>{ present: boolean \| null; status?: string \| null; scanners?: { name?: string; connected?: boolean; index?: number; identifier?: string; }[]; raw?: any; }</code> |                                                     |
| **`raw`**       | <code>any</code>                                                                                                                                                          |                                                     |
| **`timedOut`**  | <code>boolean</code>                                                                                                                                                      | Indica si hubo timeout al obtener la disponibilidad |


#### Array

| Prop         | Type                | Description                                                                                            |
| ------------ | ------------------- | ------------------------------------------------------------------------------------------------------ |
| **`length`** | <code>number</code> | Gets or sets the length of the array. This is a number one higher than the highest index in the array. |

| Method             | Signature                                                                                                                     | Description                                                                                                                                                                                                                                 |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **toString**       | () =&gt; string                                                                                                               | Returns a string representation of an array.                                                                                                                                                                                                |
| **toLocaleString** | () =&gt; string                                                                                                               | Returns a string representation of an array. The elements are converted to string using their toLocalString methods.                                                                                                                        |
| **pop**            | () =&gt; T \| undefined                                                                                                       | Removes the last element from an array and returns it. If the array is empty, undefined is returned and the array is not modified.                                                                                                          |
| **push**           | (...items: T[]) =&gt; number                                                                                                  | Appends new elements to the end of an array, and returns the new length of the array.                                                                                                                                                       |
| **concat**         | (...items: <a href="#concatarray">ConcatArray</a>&lt;T&gt;[]) =&gt; T[]                                                       | Combines two or more arrays. This method returns a new array without modifying any existing arrays.                                                                                                                                         |
| **concat**         | (...items: (T \| <a href="#concatarray">ConcatArray</a>&lt;T&gt;)[]) =&gt; T[]                                                | Combines two or more arrays. This method returns a new array without modifying any existing arrays.                                                                                                                                         |
| **join**           | (separator?: string \| undefined) =&gt; string                                                                                | Adds all the elements of an array into a string, separated by the specified separator string.                                                                                                                                               |
| **reverse**        | () =&gt; T[]                                                                                                                  | Reverses the elements in an array in place. This method mutates the array and returns a reference to the same array.                                                                                                                        |
| **shift**          | () =&gt; T \| undefined                                                                                                       | Removes the first element from an array and returns it. If the array is empty, undefined is returned and the array is not modified.                                                                                                         |
| **slice**          | (start?: number \| undefined, end?: number \| undefined) =&gt; T[]                                                            | Returns a copy of a section of an array. For both start and end, a negative index can be used to indicate an offset from the end of the array. For example, -2 refers to the second to last element of the array.                           |
| **sort**           | (compareFn?: ((a: T, b: T) =&gt; number) \| undefined) =&gt; this                                                             | Sorts an array in place. This method mutates the array and returns a reference to the same array.                                                                                                                                           |
| **splice**         | (start: number, deleteCount?: number \| undefined) =&gt; T[]                                                                  | Removes elements from an array and, if necessary, inserts new elements in their place, returning the deleted elements.                                                                                                                      |
| **splice**         | (start: number, deleteCount: number, ...items: T[]) =&gt; T[]                                                                 | Removes elements from an array and, if necessary, inserts new elements in their place, returning the deleted elements.                                                                                                                      |
| **unshift**        | (...items: T[]) =&gt; number                                                                                                  | Inserts new elements at the start of an array, and returns the new length of the array.                                                                                                                                                     |
| **indexOf**        | (searchElement: T, fromIndex?: number \| undefined) =&gt; number                                                              | Returns the index of the first occurrence of a value in an array, or -1 if it is not present.                                                                                                                                               |
| **lastIndexOf**    | (searchElement: T, fromIndex?: number \| undefined) =&gt; number                                                              | Returns the index of the last occurrence of a specified value in an array, or -1 if it is not present.                                                                                                                                      |
| **every**          | &lt;S extends T&gt;(predicate: (value: T, index: number, array: T[]) =&gt; value is S, thisArg?: any) =&gt; this is S[]       | Determines whether all the members of an array satisfy the specified test.                                                                                                                                                                  |
| **every**          | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; boolean                                 | Determines whether all the members of an array satisfy the specified test.                                                                                                                                                                  |
| **some**           | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; boolean                                 | Determines whether the specified callback function returns true for any element of an array.                                                                                                                                                |
| **forEach**        | (callbackfn: (value: T, index: number, array: T[]) =&gt; void, thisArg?: any) =&gt; void                                      | Performs the specified action for each element in an array.                                                                                                                                                                                 |
| **map**            | &lt;U&gt;(callbackfn: (value: T, index: number, array: T[]) =&gt; U, thisArg?: any) =&gt; U[]                                 | Calls a defined callback function on each element of an array, and returns an array that contains the results.                                                                                                                              |
| **filter**         | &lt;S extends T&gt;(predicate: (value: T, index: number, array: T[]) =&gt; value is S, thisArg?: any) =&gt; S[]               | Returns the elements of an array that meet the condition specified in a callback function.                                                                                                                                                  |
| **filter**         | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; T[]                                     | Returns the elements of an array that meet the condition specified in a callback function.                                                                                                                                                  |
| **reduce**         | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T) =&gt; T                           | Calls the specified callback function for all the elements in an array. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function.                      |
| **reduce**         | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T, initialValue: T) =&gt; T          |                                                                                                                                                                                                                                             |
| **reduce**         | &lt;U&gt;(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) =&gt; U, initialValue: U) =&gt; U | Calls the specified callback function for all the elements in an array. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function.                      |
| **reduceRight**    | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T) =&gt; T                           | Calls the specified callback function for all the elements in an array, in descending order. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function. |
| **reduceRight**    | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T, initialValue: T) =&gt; T          |                                                                                                                                                                                                                                             |
| **reduceRight**    | &lt;U&gt;(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) =&gt; U, initialValue: U) =&gt; U | Calls the specified callback function for all the elements in an array, in descending order. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function. |


#### ConcatArray

| Prop         | Type                |
| ------------ | ------------------- |
| **`length`** | <code>number</code> |

| Method    | Signature                                                          |
| --------- | ------------------------------------------------------------------ |
| **join**  | (separator?: string \| undefined) =&gt; string                     |
| **slice** | (start?: number \| undefined, end?: number \| undefined) =&gt; T[] |


#### ScanEvent

| Prop            | Type                |
| --------------- | ------------------- |
| **`data`**      | <code>string</code> |
| **`labelType`** | <code>string</code> |
| **`source`**    | <code>string</code> |


### Type Aliases


#### ScannerStatus

<code>'WAITING' | 'SCANNING' | 'DISABLED' | 'CONNECTED' | 'DISCONNECTED' | string</code>


#### RemoveListener

Función para remover un listener de escaneo

<code>(): void</code>


#### ScanResultCallback

Callback para recibir resultados de escaneo

<code>(result: <a href="#scanevent">ScanEvent</a>): void</code>

</docgen-api>

## License

**BSD-3-Clause**

```
Copyright (c) 2021-2023, jkbz64 and contributors
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
