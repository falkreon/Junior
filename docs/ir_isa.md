# EsoVM

This may be interpreted directly as a virtual machine, or converted into platform machine code. Mnemonics and grammar shown here are for the Jarser (flat text file) version, but the internal and binary representations hew very close to this textual description.


## Data Types

* i8, i16, i32, i64 - integer types
* f16, f32, f64 - floating point types
* addr - address. This is an alias for some integer type i8 or larger which is equivalent to the target's address type


floating point types are always signed


integer types, including addr, are neither signed nor unsigned. Integer instructions may be signed or unsigned.


## Registers

The VM has a program counter, a branch pointer to the return address at the start of the stack frame, and a stack pointer to the last element pushed onto the stack. It also has a local variable table, which is modeled after stack access but may be implemented by equivalent register operations.


The local variable table can be thought of as having pools of registers of each data type: there is an i32 slot zero, and an f64 slot zero, and these are different local variables. The instruction used determines which data type is accessed.


## Instructions

### Load/Store

```
r = r/m/i/c ; 0x01
r/m = r    ; 0x02

(alternately)

r = load r/m/i/c
r/m = store r
```

### Stack
```
push r/i/c  ; 0x03
r = pop     ; 0x04
```

### Allocation
```
r = allocate r/i/c       ; 0x10 - r/i is the quantity to be allocated, return type r is "addr"
free r                   ; 0x11

r = allocate stack r/i/c ; 0x12 - r/i is the quantity to be allocated, return type r is "addr"
free stack r             ; 0x13
```

### ALU operations - per IEEE1394, and are phrased as register assignments.

```
r = r + r/i/c ; 0x20
r = r - r/i/c ; 0x21
r = r * r/i/c ; 0x22
r = r / r/i/c ; 0x23
r = r % r/i/c ; 0x24

(alternately)

r = add r r/i/c
r = sub r r/i/c
r = mul r r/i/c
r = div r r/i/c
r = mod r r/i/c
```

### Type Conversion
```
r = convert r ; 0x30 - this needs to be written like 'i32 %0 = convert f32 %2'. Uses no-dest/convert machine code format
```

### Misc, platform-dependant
```
interrupt i ; 0xF0 trigger a hardware interrupt
out r/i     ; 0xF1 port write
r = in r/i  ; 0xF2 port read
halt        ; 0xFF
```

## Example

```
; (3 + 2) * 4 + (12 * 6)
var x : i32
var y : i32
x = 3
x = x + 2
x = x * 4
y = 12
y = y * 6
x = x + y

; x == 92
```

## Bytecode

```
Opcodes: 1 byte

Operand Types: 4 bits
    0x0 register
    0x1 immediate value
    0x2 constant pool offset (addr)
    0x3 value at constant pool offset
    
    0x4-0xF memory addressing modes
    
    0x4 register base address plus immediate offset
    0x5 register base address plus constant pool offset
    0x6 register base plus register offset
    0x7 indirect register base plus immediate offset
	0x8 indirect register base plus constant offset
	0x9 indirect register base address plus register offset
	
	"indirect" means that when the memory location is found, it's treated as an address, and that address is used.

3-operand instruction: 1 byte opcode, 4bit-4bit operand types, 8/8/32 operand bits.
The type of the first operand and the destination are always the same

 byte    0          1             2             3          4          5          6          7          8
[opcode   ][type|type][destination ][firstoperand][secondoperand                                        ]

No-dest or convert instruction: 1 byte opcode, 4bit-4bit operand types, 1 byte first operand, 5 bytes second operand
 byte    0          1             2             3          4          5          6          7          8
[opcode   ][type|type][firstoperand][secondoperand                                                      ]
```
