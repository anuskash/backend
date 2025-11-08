-- Seed prohibited keywords for student marketplace

-- DRUGS (High severity - auto reject)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('marijuana', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('weed', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('cannabis', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('thc', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('cbd', 'drugs', 'high', 'reject', 'Controlled substance', GETDATE(), 1),
('cocaine', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('heroin', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('meth', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('mdma', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('ecstasy', 'drugs', 'high', 'reject', 'Illegal substance', GETDATE(), 1),
('pills', 'drugs', 'medium', 'flag', 'Potential illegal drugs', GETDATE(), 1),
('prescription drugs', 'drugs', 'high', 'reject', 'Illegal sale of prescription medication', GETDATE(), 1);

-- WEAPONS (High severity - auto reject)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('gun', 'weapons', 'high', 'reject', 'Weapon', GETDATE(), 1),
('pistol', 'weapons', 'high', 'reject', 'Weapon', GETDATE(), 1),
('rifle', 'weapons', 'high', 'reject', 'Weapon', GETDATE(), 1),
('firearm', 'weapons', 'high', 'reject', 'Weapon', GETDATE(), 1),
('weapon', 'weapons', 'high', 'reject', 'General weapon', GETDATE(), 1),
('knife', 'weapons', 'medium', 'flag', 'Potential weapon', GETDATE(), 1),
('blade', 'weapons', 'medium', 'flag', 'Potential weapon', GETDATE(), 1),
('ammunition', 'weapons', 'high', 'reject', 'Weapon related', GETDATE(), 1),
('explosive', 'weapons', 'high', 'reject', 'Dangerous item', GETDATE(), 1);

-- ALCOHOL (High severity - auto reject for student marketplace)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('alcohol', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('beer', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('wine', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('vodka', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('whiskey', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('rum', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1),
('liquor', 'alcohol', 'high', 'reject', 'Restricted item for students', GETDATE(), 1);

-- TOBACCO & VAPING (High severity - auto reject)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('cigarette', 'tobacco', 'high', 'reject', 'Tobacco product', GETDATE(), 1),
('tobacco', 'tobacco', 'high', 'reject', 'Tobacco product', GETDATE(), 1),
('vape', 'tobacco', 'high', 'reject', 'Vaping product', GETDATE(), 1),
('vaping', 'tobacco', 'high', 'reject', 'Vaping product', GETDATE(), 1),
('e-cigarette', 'tobacco', 'high', 'reject', 'Vaping product', GETDATE(), 1),
('juul', 'tobacco', 'high', 'reject', 'Vaping brand', GETDATE(), 1),
('nicotine', 'tobacco', 'high', 'reject', 'Tobacco product', GETDATE(), 1);

-- SCAM INDICATORS (Medium severity - flag for review)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('100% legit', 'scam_indicators', 'medium', 'flag', 'Common scam phrase', GETDATE(), 1),
('guaranteed', 'scam_indicators', 'low', 'flag', 'Suspicious claim', GETDATE(), 1),
('no refunds', 'scam_indicators', 'medium', 'flag', 'Suspicious terms', GETDATE(), 1),
('cash only', 'scam_indicators', 'medium', 'flag', 'Potential scam', GETDATE(), 1),
('wire transfer', 'scam_indicators', 'high', 'flag', 'Scam payment method', GETDATE(), 1),
('send money first', 'scam_indicators', 'high', 'flag', 'Scam tactic', GETDATE(), 1),
('trust me', 'scam_indicators', 'medium', 'flag', 'Suspicious phrase', GETDATE(), 1);

-- PROFANITY (Low severity - flag)
INSERT INTO prohibited_keywords (keyword, category, severity, auto_action, description, added_date, is_active) VALUES
('fuck', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1),
('shit', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1),
('bitch', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1),
('ass', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1),
('damn', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1),
('bastard', 'profanity', 'low', 'flag', 'Inappropriate language', GETDATE(), 1);

PRINT 'Successfully seeded ' + CAST(@@ROWCOUNT AS VARCHAR) + ' prohibited keywords!';
