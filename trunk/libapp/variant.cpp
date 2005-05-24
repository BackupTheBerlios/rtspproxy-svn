/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "variant.h"

#include "dbg.h"

CVariant::CVariant(void):m_type(VT_NONE)
{
	// Empty
}

CVariant::CVariant(Type t):
m_type(t)
{
	if (t == VT_STRING)
		m_val.sval = NULL;
}

CVariant::CVariant(const CVariant & other)
{
	m_type = other.m_type;
	switch (m_type) {
	case VT_NONE:
		break;
	case VT_BOOL:
		m_val.bval = other.m_val.bval;
		break;
	case VT_INT32:
		m_val.ival = other.m_val.ival;
		break;
	case VT_UINT32:
		m_val.uval = other.m_val.uval;
		break;
	case VT_FLOAT:
		m_val.fval = other.m_val.fval;
		break;
	case VT_STRING:
		m_val.sval = new char[1 + strlen(other.m_val.sval)];
		strcpy(m_val.sval, other.m_val.sval);
		break;
	default:
		assert(false);
	}
}

CVariant::~CVariant(void)
{
	Destroy();
}

CVariant & CVariant::operator=(const CVariant & other)
{
	Destroy();
	m_type = other.m_type;
	switch (m_type) {
	case VT_NONE:
		break;
	case VT_BOOL:
		m_val.bval = other.m_val.bval;
		break;
	case VT_INT32:
		m_val.ival = other.m_val.ival;
		break;
	case VT_UINT32:
		m_val.uval = other.m_val.uval;
		break;
	case VT_FLOAT:
		m_val.fval = other.m_val.fval;
		break;
	case VT_STRING:
		m_val.sval = new char[1 + strlen(other.m_val.sval)];
		strcpy(m_val.sval, other.m_val.sval);
		break;
	default:
		assert(false);
	}
	return *this;
}

void CVariant::Destroy(void)
{
	if (m_type == VT_STRING) {
		delete[]m_val.sval;
		m_val.sval = NULL;
	}
	m_type = VT_NONE;
}

CVariant::Type CVariant::GetType(void)
{
	return m_type;
}

void CVariant::SetType(Type t)
{
	Destroy();
	m_type = t;
	if (t == VT_STRING)
		m_val.sval = NULL;
}

bool CVariant::GetBool(void) const
{
	assert(m_type == VT_BOOL);
	return m_val.bval;
}

void CVariant::SetBool(bool b)
{
	assert(m_type == VT_BOOL);
	m_val.bval = b;
}

INT32 CVariant::GetInt(void) const
{
	assert(m_type == VT_INT32);
	return m_val.ival;
}

void CVariant::SetInt(INT32 i)
{
	assert(m_type == VT_INT32);
	m_val.ival = i;
}

UINT32 CVariant::GetUint(void) const
{
	assert(m_type == VT_UINT32);
	return m_val.uval;
}

void CVariant::SetUint(UINT32 u)
{
	assert(m_type == VT_UINT32);
	m_val.uval = u;
}

float CVariant::GetFloat(void) const
{
	assert(m_type == VT_FLOAT);
	return m_val.fval;
}

void CVariant::SetFloat(float f)
{
	assert(m_type == VT_FLOAT);
	m_val.fval = f;
}

CPCHAR CVariant::GetString(void) const
{
	assert(m_type == VT_STRING);
	return m_val.sval;
}

void CVariant::SetString(CPCHAR s)
{
	assert(m_type == VT_STRING);
	delete[]m_val.sval;
	m_val.sval = new char[strlen(s) + 1];
	strcpy(m_val.sval, s);
}

bool CVariant::operator==(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			ret = (m_val.bval == other.m_val.bval);
			break;
		case VT_INT32:
			ret = (m_val.ival == other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval == other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval == other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

bool CVariant::operator!=(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			ret = (m_val.bval != other.m_val.bval);
			break;
		case VT_INT32:
			ret = (m_val.ival != other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval != other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval != other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

bool CVariant::operator<=(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			break;
		case VT_INT32:
			ret = (m_val.ival <= other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval <= other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval <= other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

bool CVariant::operator>=(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			break;
		case VT_INT32:
			ret = (m_val.ival >= other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval >= other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval >= other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

bool CVariant::operator<(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			break;
		case VT_INT32:
			ret = (m_val.ival < other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval < other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval < other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

bool CVariant::operator>(const CVariant & other) const
{
	bool ret = false;
	if (m_type == other.m_type && m_type != VT_NONE
	    && m_type != VT_STRING) {
		switch (m_type) {
		case VT_BOOL:
			break;
		case VT_INT32:
			ret = (m_val.ival > other.m_val.ival);
			break;
		case VT_UINT32:
			ret = (m_val.uval > other.m_val.uval);
			break;
		case VT_FLOAT:
			ret = (m_val.fval > other.m_val.fval);
			break;
		default:
			assert(false);
		}
	}
	return ret;
}

/** LOG **
 *
 * $Log: variant.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

