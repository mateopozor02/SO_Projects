// VirtualMemoryManagerV0.java
package vmmanager;

import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

public class VirtualMemoryManagerV0 {

    MainMemory memory;
    int numBitsToAddress;

    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    boolean memoryContentFlag = true;

    public VirtualMemoryManagerV0(MainMemory mainMemory) throws MemoryException {
        this.memory = mainMemory;
        numBitsToAddress = log2(mainMemory.size());
    }

    public void writeByte(Integer binaryString, Byte data) throws MemoryException {
        int physicalAddress = BitwiseToolbox.extractBits(binaryString, 0, numBitsToAddress - 1);
        memory.writeByte(physicalAddress, data);

        String fullAddress = String.format("%" + numBitsToAddress + "s", Integer.toBinaryString(physicalAddress)).replace(' ', '0');

        System.out.println("RAM write: @" + fullAddress + " <-- " + data);
    }

    public Byte readByte(Integer binaryString) throws MemoryException {
        int physicalAddress = BitwiseToolbox.extractBits(binaryString, 0, numBitsToAddress - 1);
        byte valueAtAddress = memory.readByte(physicalAddress);

        if (memoryContentFlag) {
            String fullAddress = String.format("%" + numBitsToAddress + "s", Integer.toBinaryString(physicalAddress)).replace(' ', '0');
            System.out.println("RAM read: @" + fullAddress + " --> " + valueAtAddress);
        }

        return valueAtAddress;
    }

    public void printMemoryContent() throws MemoryException {
        memoryContentFlag = false;

        for (int i = 0; i < memory.size(); i++) {
            System.out.println(BitwiseToolbox.getBitString(i, numBitsToAddress - 1) + ": " + readByte(i));
        }
        
        memoryContentFlag = true;
    }
}
