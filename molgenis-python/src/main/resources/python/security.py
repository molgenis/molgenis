from getpass import getpass
from pbkdf2 import PBKDF2
import os
import platform
if platform.system() == 'Darwin':
    import crypto
    import sys
    sys.modules['Crypto'] = crypto
from Crypto.Cipher import AES
import base64
import pickle

### Settings ###
saltSeed_length = 20
saltSeed = 'TRNB6AWH18JQVMU3IX9U'

PASSPHRASE_FILE = os.path.expanduser('~')+os.sep+'secret.p'
SECRETSDB_FILE = os.path.expanduser('~')+os.sep+'secrets'
PASSPHRASE_SIZE = 64 # 512-bit passphrase
KEY_SIZE = 32 # 256-bit key
BLOCK_SIZE = 16  # 16-bit blocks
IV_SIZE = 16 # 128-bits to initialise
SALT_SIZE = 8 # 64-bits of salt

def overwrite_passphrase_location(folder):
    global PASSPHRASE_FILE
    global SECRETSDB_FILE
    if '~' in folder:
        folder =  os.path.expanduser(folder)
    PASSPHRASE_FILE = folder+os.sep+'secret.p'
    SECRETSDB_FILE = folder+os.sep+'secrets'
    init()
### System Functions ###

def getSaltForKey(key):
    return PBKDF2(key, saltSeed).read(SALT_SIZE) # Salt is generated as the hash of the key with it's own salt acting like a seed value

def encrypt(plaintext, salt):
    ''' Pad plaintext, then encrypt it with a new, randomly initialised cipher. Will not preserve trailing whitespace in plaintext!'''

    # Initialise Cipher Randomly
    initVector = os.urandom(IV_SIZE)

    # Prepare cipher key:
    key = PBKDF2(passphrase, salt).read(KEY_SIZE)

    cipher = AES.new(key, AES.MODE_CBC, initVector) # Create cipher

    return initVector + cipher.encrypt(plaintext + ' '*(BLOCK_SIZE - (len(plaintext) % BLOCK_SIZE))) # Pad and encrypt

def decrypt(ciphertext, salt):
    ''' Reconstruct the cipher object and decrypt. Will not preserve trailing whitespace in the retrieved value!'''

    # Prepare cipher key:
    key = PBKDF2(passphrase, salt).read(KEY_SIZE)

    # Extract IV:
    initVector = ciphertext[:IV_SIZE]
    ciphertext = ciphertext[IV_SIZE:]

    cipher = AES.new(key, AES.MODE_CBC, initVector) # Reconstruct cipher (IV isn't needed for edecryption so is set to zeros)
    return cipher.decrypt(ciphertext).decode('ascii').rstrip(' ') # Decrypt and depad


### User Functions ###

def store(key, value):
    ''' Sore key-value pair safely and save to disk.'''
    global db

    db[key] = encrypt(value, getSaltForKey(key))
    with open(SECRETSDB_FILE, 'wb') as f:
        pickle.dump(db, f)

def retrieve(key):
    ''' Fetch key-value pair.'''
    return decrypt(db[key], getSaltForKey(key))

def require_username(key):
    ''' Test if key is stored, if not, prompt the user for it while hiding their input from shoulder-surfers.'''
    if not key in db: store(key, getpass('Please write the server username:'))

def require_password(key):
    ''' Test if key is stored, if not, prompt the user for it while hiding their input from shoulder-surfers.'''
    if not key in db: store(key, getpass('Please write the server password:'))

def remove_secrets_file():
    try:
        os.remove(PASSPHRASE_FILE)
    except OSError:
        pass
    try:
        os.remove(SECRETSDB_FILE)
    except OSError:
        pass
passphrase = None
db = None
def init():
    global db
    global passphrase
    try:
        with open(PASSPHRASE_FILE,'rb') as f:
            passphrase = f.read()
        if len(passphrase) == 0: raise IOError
    except IOError:
        with open(PASSPHRASE_FILE, 'wb') as f:
            passphrase = os.urandom(PASSPHRASE_SIZE) # Random passphrase
            f.write(base64.b64encode(passphrase))
            try: os.remove(SECRETSDB_FILE) # If the passphrase has to be regenerated, then the old secrets file is irretrievable and should be removed
            except: pass
    else:
        passphrase = base64.b64decode(passphrase) # Decode if loaded from already extant file
    
    # Load or create secrets database:
    try:
        with open(SECRETSDB_FILE,'rb') as f:
            db = pickle.load(f)
        if db == {}: raise IOError
    except (IOError, EOFError):
        db = {}
        with open(SECRETSDB_FILE, 'wb') as f:
            pickle.dump(db, f)
init()

