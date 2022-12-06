package cf.mgorbunov;

import java.util.ArrayList;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.gc.g1.G1CollectedHeap;
import sun.jvm.hotspot.gc.g1.HeapRegion;
import sun.jvm.hotspot.gc.g1.HeapRegionManager;
import sun.jvm.hotspot.gc.shared.CollectedHeap;
import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.oops.Array;
import sun.jvm.hotspot.oops.ByteField;
import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.IndexableFieldIdentifier;
import sun.jvm.hotspot.oops.ObjectHistogram;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.TypeArray;
import sun.jvm.hotspot.oops.TypeArrayKlass;
import sun.jvm.hotspot.runtime.BasicType;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;
import sun.jvm.hotspot.utilities.AddressOps;

public class OldRegionHeapScanner extends Tool {
    private static boolean printByteArrays = false;

    public static void main(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();
        for (String arg : args) {
            if ("--print-byte-arrays".equals(arg)) {
                printByteArrays = true;
            } else {
                newArgs.add(arg);
            }
        }
        new OldRegionHeapScanner().execute(newArgs.toArray(args));
    }

    @Override
    public void run() {
        try {
            VM vm = VM.getVM();
            Universe universe = vm.getUniverse();
            CollectedHeap heap = universe.heap();

            if (heap instanceof G1CollectedHeap) {
                G1CollectedHeap g1CollectedHeap = (G1CollectedHeap) heap;
                HeapRegionManager heapRegionManager = g1CollectedHeap.hrm();

                ObjectHistogram objectHistogram = new ObjectHistogram() {
                    @Override
                    public boolean doObj(Oop obj) {
                        HeapRegion region = heapRegionManager.getByAddress(obj.getHandle());
                        if (region.isHumongous() || region.isOld()) {
                            handleTypeArray(obj, vm);
                            super.doObj(obj);
                            return true;
                        }
                        return false;
                    }
                };
                vm.getObjectHeap().iterate(objectHistogram);
                objectHistogram.print();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleTypeArray(Oop obj, VM vm) {
        if (printByteArrays) {
            if (obj instanceof TypeArray) {
                TypeArray typeArray = (TypeArray) obj;
                long length = typeArray.getLength();
                System.out.println("================================================Byte array size: " + length);
                TypeArrayKlass typeArrayKlass = (TypeArrayKlass) typeArray.getKlass();
                if (TypeArrayKlass.T_BYTE == typeArrayKlass.getElementType()) {
                    byte[] arr = new byte[(int) length];
                    for (int index = 0; index < length; index++) {
                        FieldIdentifier id = new IndexableFieldIdentifier(index);
                        long offset = Array.baseOffsetInBytes(BasicType.T_BYTE) + index * vm.getObjectHeap().getByteSize();
                        ByteField byteField = new ByteField(id, offset, false);
                        arr[index] = byteField.getValue(obj);
                    }
                    System.out.println(new String(arr));
                    System.out.println("================================================");
                }
            }
        }
    }

    static class HeapRegionBounds {
        Address bottom;
        Address end;

        public HeapRegionBounds(Address bottom, Address end) {
            this.bottom = bottom;
            this.end = end;
        }
    }
}